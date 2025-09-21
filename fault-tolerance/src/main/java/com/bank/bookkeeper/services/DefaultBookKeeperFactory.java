package com.bank.bookkeeper.services;

import org.apache.bookkeeper.client.BookKeeper;

public class DefaultBookKeeperFactory implements BookKeeperFactory {

    @Override
    public BookKeeper create(String zkConnect) throws Exception {
        return new BookKeeper(zkConnect);
    }
}
