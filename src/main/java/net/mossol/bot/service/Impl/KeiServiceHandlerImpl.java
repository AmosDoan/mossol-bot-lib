package net.mossol.bot.service.Impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.mossol.bot.service.KeiServiceHandler;

import com.linecorp.centraldogma.client.Watcher;
import org.springframework.util.CollectionUtils;

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
        return "멍멍! CS 부탁해요! : " + keiUnitMember.get(index);
    }

    @Override
    public String getRandomMember(List<String> infoList) {
        String memberList = infoList.get(0);
        int count = Integer.valueOf(infoList.get(1));

        List<String> members = Arrays.asList(memberList.split(","));

        if (CollectionUtils.isEmpty(members)) {
            return "멍멍! 멤버 리스트가 이상해요!";
        }

        if (count > members.size() || count <= 0) {
            return "멍멍! 골라야하는 분들 수가 이상해yo!";
        }

        StringBuilder builder = new StringBuilder().append("멍멍!! ");
        Collections.shuffle(members);

        builder.append(members.subList(0, count));
        return builder.toString();
    }
}
