package net.mossol.bot.bot.service.Impl;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.mossol.bot.bot.service.KeiServiceHandler;

import com.linecorp.centraldogma.client.Watcher;

@Service
public class KeiServiceHandlerImpl implements KeiServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(KeiServiceHandlerImpl.class);

    private final Random random = new Random();

    @Resource
    private Watcher<List<String>> keiUnitWatcher;

    private volatile List<String> keiUnitMember;
    private static final List<String> keiUnitDefaultMember = Arrays.asList("김도한", "석영태", "이경찬", "한승욱");

    @PostConstruct
    private void init() throws InterruptedException {
        keiUnitMember = keiUnitDefaultMember;

        keiUnitWatcher.watch((revision, member) -> {
            if (member== null)  {
                logger.warn("Kei Member Watch Failed");
                return;
            }
            logger.info("Kei Member Updated : " + member);
            keiUnitMember = member;
        });

        try {
            keiUnitWatcher.awaitInitialValue(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Failed fetch Kei Member from Central Dogma; Set the Default Member");
        }
    }

    @Override
    public String getCSLotto() {
        final int index = (random.nextInt() & Integer.MAX_VALUE) % keiUnitMember.size();
        return keiUnitMember.get(index);
    }
}
