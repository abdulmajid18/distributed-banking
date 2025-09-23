//package com.bank.bookkeeper.services;
//
//import com.bank.bookkeeper.LoadConfig;
//import org.apache.bookkeeper.client.BookKeeper;
//import org.apache.bookkeeper.client.LedgerHandle;
//import org.apache.bookkeeper.client.api.BKException;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.slf4j.Logger;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
//
//@ExtendWith(MockitoExtension.class)
//class BookKeeperServiceTest {
//
//    @Mock
//    private LoadConfig config;
//
//    @Mock
//    private Logger logger;
//
//    @Mock
//    private BookKeeper bookKeeper;
//
//    @Mock
//    private BookKeeperFactory factory;
//
//    @Mock
//    private BookKeeper mockClient;
//
//    @Mock
//    private LedgerHandle ledgerHandle;
//
//    private BookKeeperService bookKeeperService;
//
//
//    @BeforeEach
//    void setUp() {
//        bookKeeperService = new BookKeeperService(config, factory);
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    void testConnect_Success() throws Exception {
//        // when
//        when(config.getHost()).thenReturn("localhost");
//        when(config.getPort()).thenReturn("2181");
//        when(factory.create("localhost:2181")).thenReturn(mockClient);
//
//        bookKeeperService.connect();
//
//        //assert
//        verify(config, times(1)).getHost();
//        verify(config, times(1)).getPort();
//
//    }
//
//    @Test
//    void testConnect_WithConfigValues() throws Exception {
//        // Arrange
//        when(config.getHost()).thenReturn("zk-server");
//        when(config.getPort()).thenReturn("2182");
//
//
//        // Act
//        bookKeeperService.connect();
//
//        // Assert
//        verify(config).getHost();
//        verify(config).getPort();
//    }
//
//    @Test
//    void testConnect_ThrowsIOException() throws Exception {
//        // Arrange
//        when(config.getHost()).thenReturn("localhost");
//        when(config.getPort()).thenReturn("2181");
//
//        // Mock BookKeeper constructor to throw IOException
//        when(factory.create("localhost:2181"))
//                .thenThrow(new IOException("Failed to connect"));
//        assertThrows(IOException.class, () -> bookKeeperService.connect());
//    }
//
//    @Test
//    void testCreateLedger_Success() throws Exception {
//        // Arrange
//        when(config.getHost()).thenReturn("localhost");
//        when(config.getPort()).thenReturn("2181");
//        when(factory.create("localhost:2181")).thenReturn(mockClient);
//
//        bookKeeperService.connect();
//
//        when(config.getLedgerPassword()).thenReturn("secret");
//        when(config.getLedgerName()).thenReturn("test-ledger");
//        when(mockClient.createLedger(
//                anyInt(), anyInt(), anyInt(),
//                any(BookKeeper.DigestType.class),
//                any(byte[].class),
//                anyMap()
//        )).thenReturn(ledgerHandle);
//
//        when(ledgerHandle.getId()).thenReturn(123L);
//
//        // Act
//      bookKeeperService.createLedger();
//
//        // Assert
//        verify(mockClient).createLedger(
//                eq(1), eq(1), eq(1),
//                eq(BookKeeper.DigestType.MAC),
//                eq("secret".getBytes()),
//                anyMap()
//        );
//        verify(config, times(2)).getLedgerName();
//        verify(ledgerHandle, times(1)).getId();
//    }
//
//    @Test
//    void testClose_WithException() throws Exception {
//        // Arrange
//        when(config.getHost()).thenReturn("localhost");
//        when(config.getPort()).thenReturn("2181");
//        when(factory.create("localhost:2181")).thenReturn(mockClient);
//
//        bookKeeperService.connect();
//
//        doThrow(new RuntimeException("close failed")).when(mockClient).close();
//
//        // Act
//        bookKeeperService.close();
//
//        // Assert
//        verify(mockClient).close(); // still attempted
//    }
//
//}