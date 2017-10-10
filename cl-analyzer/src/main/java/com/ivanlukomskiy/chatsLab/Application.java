package com.ivanlukomskiy.chatsLab;

import com.ivanlukomskiy.chatsLab.service.GatheringJob;
import com.ivanlukomskiy.chatsLab.service.Job;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Comparator;
import java.util.List;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
@SpringBootApplication
public class Application {
    private static final Logger logger = LogManager.getLogger(GatheringJob.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private List<Job> jobs;

    @EventListener({ContextRefreshedEvent.class})
    void contextRefreshedEvent() {
        jobs.sort(Comparator.comparing(Job::getPriority));
        jobs.forEach(job->{
            logger.info("Job to be executed: {}", job.getDescription());
            job.run();
        });
    }

    @Bean
    @Scope("prototype")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(4);
        pool.setMaxPoolSize(4);
        return pool;
    }
}
