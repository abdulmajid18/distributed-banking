package com.bank.bookkeeper.services;

import com.bank.bookkeeper.LoadConfig;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.LedgerHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class BookKeeperService {
    private static final Logger logger = LoggerFactory.getLogger(BookKeeperService.class);

    private final LoadConfig config;
    private final BookKeeperFactory factory;
    private BookKeeper client;

    public BookKeeperService(LoadConfig config, BookKeeperFactory factory) {
        this.config = config;
        this.factory = factory;
    }

    public void connect() throws Exception {
        String zkConnect = config.getHost() + ":" + config.getPort();
        client = factory.create(zkConnect);
        logger.info("Connected to BookKeeper at {}", zkConnect);
    }

    public LedgerHandle createLedger() throws BKException, InterruptedException {
        LedgerHandle lh = client.createLedger(
                1, 1, 1,
                BookKeeper.DigestType.MAC,
                config.getLedgerPassword().getBytes(),
                Collections.singletonMap("name", config.getLedgerName().getBytes())
        );
        logger.info("Created ledger with ID: {} and name: {}", lh.getId(), config.getLedgerName());
        return lh;
    }

    public void close() {
        if (client != null) {
            try {
                client.close();
                logger.info("BookKeeper connection closed.");
            } catch (Exception e) {
                logger.error("Error closing BookKeeper", e);
            }
        }
    }
}
