package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.gui.DownloadingStatusListener;
import com.ivanlukomskiy.chatsLab.model.ChatGuiDto;
import com.ivanlukomskiy.chatsLab.model.Credentials;
import com.ivanlukomskiy.chatsLab.model.pack.ChatDto;
import com.ivanlukomskiy.chatsLab.model.pack.MessageDto;
import com.ivanlukomskiy.chatsLab.model.pack.UserDto;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.messages.Dialog;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.messages.MessagesGetHistoryQuery;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.ivanlukomskiy.chatsLab.service.IOService.INSTANCE;
import static com.ivanlukomskiy.chatsLab.util.LocalizationHolder.localization;

/**
 * Provides methods to access VK API and download messages
 * @author ivan_l
 */
public class VkService {

    private static final Logger logger = LogManager.getLogger(VkService.class);

    private static final int APP_ID = 5778688;
    private static final String CLIENT_SECRET = "H9iRE6A6SwrNxylFnFoC";
    private static final String PERMISSIONS = "messages";
    private static final String TARGET_URI = "https://oauth.vk.com/authorize";
    private static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";
    private static final int RETRY_DELAY = 5;
    private static final int MAX_RETRIES = 10;

    private static final int RECORDS_PER_PAGE_MAX = 200;
    private static final int MAX_USERS_PER_REQUEST = 100;
    private static final long MIN_REQUESTS_DELAY = 500; // min delay between requests in milliseconds

    private Dumper dumper = new JsonDumper();

    private UserActor getActor() {
        return new UserActor(INSTANCE.getId(), INSTANCE.getPassword());
    }

    public List<ChatGuiDto> loadDialogues() throws ClientException, InterruptedException, ApiException {

        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = new UserActor(INSTANCE.getId(), INSTANCE.getPassword());

        List<ChatGuiDto> chats = new ArrayList<>();

        int page = 0;

        while (true) {
            long requestSentTime = System.currentTimeMillis();

            logger.debug("Loading dialogues, page " + page);

            List<Dialog> dialogues = requestMessages(vk, page, actor);

            for (Dialog dialog : dialogues) {
                Message message = dialog.getMessage();
                // Skip personal and group messages
                if (message.getChatId() == null) {
                    continue;
                }
                chats.add(new ChatGuiDto(message.getChatId(), message.getTitle(), dialog.getMessage().getAdminId()));
            }

            long timeToSleep = MIN_REQUESTS_DELAY - System.currentTimeMillis() + requestSentTime;
            if (timeToSleep > 0) {
                Thread.sleep(timeToSleep);
            }
            if (dialogues.size() < RECORDS_PER_PAGE_MAX) {
                break;
            }

            page++;
        }
        return chats;
    }

    private List<Dialog> requestMessages(VkApiClient vk, int page, UserActor actor)
            throws ClientException, ApiException {

        return requestMessages(vk, page, actor, 0);
    }

    private List<Dialog> requestMessages(VkApiClient vk, int page, UserActor actor, int retries)
            throws ClientException, ApiException {

        try {
            return vk.messages()
                    .getDialogs(actor)
                    .count(RECORDS_PER_PAGE_MAX)
                    .offset(RECORDS_PER_PAGE_MAX * page)
                    .execute()
                    .getItems();
        } catch (ApiException e) {
            if (retries >= MAX_RETRIES) {
                throw e;
            }
            logger.error("API request failed. Will retry in {} seconds", RETRY_DELAY);
            try {
                Thread.sleep(RETRY_DELAY * 1000);
            } catch (InterruptedException ie) {
            }
            return requestMessages(vk, page, actor, retries + 1);
        }
    }

    private Map<Integer, UserDto> idToName = new HashMap<>();

    private Map<Integer, UserDto> getUsersById(Set<Integer> ids, VkApiClient vk,
                                               DownloadingStatusListener listener)
            throws ClientException, ApiException, InterruptedException {

        logger.debug("Requested {} users", ids.size());

        Set<Integer> notResolved = new HashSet<>(ids);
        notResolved.removeAll(idToName.keySet());

        logger.debug("Loading {} users", ids.size());

        // All user IDs was already queried
        if (notResolved.isEmpty()) {
            return idToName;
        }

        int index = 0;
        List<String> idQuery = new ArrayList<>();
        for (Integer id : notResolved) {

            idQuery.add(id.toString());
            index++;

            if (index % MAX_USERS_PER_REQUEST == 0 || index == notResolved.size()) {
                listener.changeText(localization.getText("downloading.users", index, notResolved.size()));
                logger.debug("Writing user details {}/{}", index, notResolved.size());

                long requestSentTime = System.currentTimeMillis();

                List<UserXtrCounters> users = getUsers(idQuery, vk);

                for (UserXtrCounters user : users) {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    idToName.put(user.getId(), dto);
                }

                idQuery.clear();
                long timeToSleep = MIN_REQUESTS_DELAY - System.currentTimeMillis() + requestSentTime;
                if (timeToSleep > 0) {
                    Thread.sleep(timeToSleep);
                }
            }
        }
        logger.debug("Users loading finished");
        return idToName;
    }

    private List<UserXtrCounters> getUsers(List<String> idQuery, VkApiClient vk) throws ClientException, ApiException {
        return getUsers(idQuery, vk, 0);
    }

    private List<UserXtrCounters> getUsers(List<String> idQuery, VkApiClient vk, int retries)
            throws ClientException, ApiException {
        try {
            return vk
                    .users()
                    .get()
                    .userIds(idQuery)
                    .execute();
        } catch (ApiException e) {
            if (retries >= MAX_RETRIES) {
                throw e;
            }
            logger.error("API request failed. Will retry in {} seconds", RETRY_DELAY);
            try {
                Thread.sleep(RETRY_DELAY * 1000);
            } catch (InterruptedException ie) {
            }
            return getUsers(idQuery, vk, retries + 1);
        }
    }

    public void downloadMessages(List<ChatGuiDto> chats, DownloadingStatusListener listener)
            throws InterruptedException, ClientException, ApiException {

        logger.info("Writing session started");
        dumper.prepare();

        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = getActor();

        Set<Integer> userIds = new HashSet<>();
        for (ChatGuiDto chat : chats) {
            if (!chat.isDownload()) {
                continue;
            }

            listener.changeText(localization.getText("downloading.chat", chat.getName()));

            ChatDto chatDto = new ChatDto();
            chatDto.setName(chat.getName());
            chatDto.setId(chat.getId());
            chatDto.setAdminId(chat.getAdminId());

            dumper.startWriting(chatDto);

            int page = 0;
            while (true) {
                long requestSentTime = System.currentTimeMillis();
                MessagesGetHistoryQuery query = vk.messages()
                        .getHistory(actor)
                        .count(RECORDS_PER_PAGE_MAX)
                        .offset(page * RECORDS_PER_PAGE_MAX);
                query.peerId(2000000000 + chat.getId());
                GetHistoryResponse response = getMessages(query);
                List<Message> messages = response.getItems();
                for (Message message : messages) {
                    MessageDto messageDto = new MessageDto(message.getId(), message.getFromId(), message.getDate(),
                            message.getBody());
                    dumper.writeMessage(messageDto);
                    if (!userIds.contains(message.getId())) {
                        userIds.add(message.getFromId());
                    }
                }
                listener.changeText(localization.getText("downloading.chat_detailed",
                        chat.getName(), page * RECORDS_PER_PAGE_MAX, response.getCount()));
                long timeToSleep = MIN_REQUESTS_DELAY - System.currentTimeMillis() + requestSentTime;
                if (timeToSleep > 0) {
                    Thread.sleep(timeToSleep);
                }
                if (messages.size() < RECORDS_PER_PAGE_MAX) {
                    break;
                }
                page++;
            }
            dumper.finishWriting();
            chat.setDownload(false);
        }
        logger.info("Messages writing finished. Starting to write users");
        userIds.add(actor.getId());
        Map<Integer, UserDto> usersById = getUsersById(userIds, vk, listener);
        dumper.writeUsers(usersById);

        logger.info("Users writing finished. Starting to write meta info");
        dumper.writeMetaInfo(actor.getId());

        listener.changeText(localization.getText("downloading.packaging"));
        logger.info("Packaging");
        dumper.finalize();
        logger.info("Writing session finished");
    }

    private GetHistoryResponse getMessages(MessagesGetHistoryQuery query) throws ClientException, ApiException {
        return getMessages(query, 0);
    }

    public GetHistoryResponse getMessages(MessagesGetHistoryQuery query, int retries) throws ClientException,
            ApiException {

        try {
            return query.execute();
        } catch (ApiException e) {
            if (retries >= MAX_RETRIES) {
                throw e;
            }
            logger.error("API request failed. Will retry in {} seconds", RETRY_DELAY);
            try {
                Thread.sleep(RETRY_DELAY * 1000);
            } catch (InterruptedException ie) {
            }
            return getMessages(query, retries + 1);
        }
    }

    public void requestAuthToken(String code, boolean saveToFile)
            throws ApiException, ClientException, IOException {

        requestAuthToken(code, saveToFile, 0);
    }

    public void requestAuthToken(String code, boolean saveToFile, int retries)
            throws ClientException, ApiException, IOException {

        try {
            TransportClient transportClient = HttpTransportClient.getInstance();
            VkApiClient vk = new VkApiClient(transportClient);

            UserAuthResponse authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET,
                            "https://oauth.vk.com/blank.html", code)
                    .execute();
            INSTANCE.setCredentials(new Credentials(authResponse.getUserId(), authResponse.getAccessToken()));
            if (saveToFile) {
                INSTANCE.serialize();
            }
        } catch (Exception e) {
            if (retries >= MAX_RETRIES) {
                throw e;
            }
            logger.error("API request failed. Will retry in {} seconds", RETRY_DELAY);
            try {
                Thread.sleep(RETRY_DELAY * 1000);
            } catch (InterruptedException ie) {
            }
            requestAuthToken(code, saveToFile, retries + 1);
        }
    }

    public URI getAuthUri() {
        try {
            URIBuilder ub = new URIBuilder(TARGET_URI);
            ub.addParameter("client_id", String.valueOf(APP_ID));
            ub.addParameter("redirect_uri", REDIRECT_URI);
            ub.addParameter("display", "page");
            ub.addParameter("scope", PERMISSIONS);
            ub.addParameter("response_type", "code");
            ub.addParameter("v", "5.60");
            return ub.build();
        } catch (URISyntaxException e) {
            System.err.println("Fatal: Unable to parse URI");
            System.exit(1);
            return null;
        }
    }
}
