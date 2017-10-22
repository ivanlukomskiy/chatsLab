package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Credentials;
import com.ivanlukomskiy.chatsLab.model.Gender;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ivanlukomskiy.chatsLab.service.IOService.INSTANCE;
import static com.ivanlukomskiy.chatsLab.service.VkService.*;
import static com.vk.api.sdk.httpclient.HttpTransportClient.getInstance;
import static java.util.stream.Collectors.toMap;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 20.10.2017.
 */
public class GenderResolver {
    public static Map<Integer, Gender> resolve(Set<Integer> userIds)
            throws IOException, ClientException, ApiException {

        TransportClient transportClient = getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        Scanner scanner = new Scanner(System.in);
        URI authUri = VkService.getAuthUri();
        System.out.println("Go to this URI to get clients code:");
        System.out.println(authUri);
        System.out.print("Enter your code: ");
        String code = scanner.next();
        UserAuthResponse authResponse = vk.oauth()
                .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, "https://oauth.vk.com/blank.html", code)
                .execute();
        INSTANCE.setCredentials(new Credentials(authResponse.getUserId(), authResponse.getAccessToken()));

        List<String> idStrings = userIds.stream().map(String::valueOf).collect(Collectors.toList());
        List<UserXtrCounters> users = new VkService().getUsers(idStrings, vk);
        return users.stream().collect(toMap(UserXtrCounters::getId, user -> getGenderBySex(user.getSex())));
    }
}
