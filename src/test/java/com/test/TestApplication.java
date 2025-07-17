package com.test;

import com.lwf.ytlivechatanalyse.dao.AuthorInfoMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestApplication {

    @Autowired
    AuthorInfoMapper authorInfoMapper;
    private final Logger logger = LoggerFactory.getLogger(TestApplication.class);

    @Test
    void updateBlocked() {
        String liveDate = "2025-07-16";
        int blockedCount = 0;
        blockedCount += authorInfoMapper.updateAuthorInfoBlockedName();
        blockedCount += authorInfoMapper.updateAuthorInfoBlockedMessage();
        blockedCount += authorInfoMapper.updateLiveChatDataBlockedName(liveDate);
        blockedCount += authorInfoMapper.updateLiveChatDataBlockedMessage(liveDate);
        blockedCount += authorInfoMapper.updateLivingChatDataBlockedName(liveDate);
        blockedCount += authorInfoMapper.updateLivingChatDataBlockedMessage(liveDate);
        logger.info("{} 更新屏蔽信息完成，屏蔽条数：{}", liveDate, blockedCount);
    }

}
