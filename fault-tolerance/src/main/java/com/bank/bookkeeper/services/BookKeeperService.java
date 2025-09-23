//package com.bank.bookkeeper.services;
//
//import com.bank.bookkeeper.LoadConfig;
//import org.apache.bookkeeper.client.BKException;
//import org.apache.bookkeeper.client.BookKeeper;
//import org.apache.bookkeeper.client.LedgerHandle;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Collections;
//
//public class BookKeeperService {
//    private static final Logger logger = LoggerFactory.getLogger(BookKeeperService.class);
//
//    private final LoadConfig config;
//    private final BookKeeperFactory factory;
//    private BookKeeper bookKeeper;
//    private LedgerHandle ledgerHandle;
//
//    public BookKeeperService(LoadConfig config, BookKeeperFactory factory) {
//        this.config = config;
//        this.factory = factory;
//    }
//
//    public void connect() throws Exception {
//        String zkConnect = config.getHost() + ":" + config.getPort();
//        bookKeeper = factory.create(zkConnect);
//        logger.info("Connected to BookKeeper at {}", zkConnect);
//    }
//
//    public long appendEntry(byte[] data) throws Exception {
//        if (ledgerHandle == null) {
//            if (bookKeeper == null) {
//                connect();
//            }
//            createLedger();
//        }
//
//        long entryId = ledgerHandle.addEntry(data);
//        logger.debug("Appended entry with ID: {}", entryId);
//        return entryId;
//    }
//
//    public void createLedger() throws BKException, InterruptedException {
//        if (ledgerHandle != null) {
//            logger.warn("Ledger already created with ID: {}", ledgerHandle.getId());
//            return;
//        }
//
//        ledgerHandle = bookKeeper.createLedger(
//                1, 1, 1,
//                BookKeeper.DigestType.MAC,
//                config.getLedgerPassword().getBytes(),
//                Collections.singletonMap("name", config.getLedgerName().getBytes())
//        );
//        logger.info("Created ledger with ID: {} and name: {}", ledgerHandle.getId(), config.getLedgerName());
//    }
//
//
//    public long getLedgerId() {
//        return ledgerHandle != null ? ledgerHandle.getId() : -1;
//    }
//
//
//    public void close() {
//        if (ledgerHandle != null) {
//            try {
//                ledgerHandle.close();
//                logger.info("Ledger closed.");
//            } catch (Exception e) {
//                logger.error("Error closing ledger", e);
//            }
//        }
//
//        if (bookKeeper != null) {
//            try {
//                bookKeeper.close();
//                logger.info("BookKeeper connection closed.");
//            } catch (Exception e) {
//                logger.error("Error closing BookKeeper", e);
//            }
//        }
//    }
//}
