package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before(){
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after(){
        stockRepository.deleteAll();
    }

    @Test
    public void 재고감소(){
        //given
        //100 - 1 = 99;
        //when
        Stock stock = stockRepository.findById(1L).orElseThrow();
        stockService.decrease(1L, 1L);
        //then
        assertThat(stock.getQuantity()).isEqualTo(99L);
    }

    @Test
    public void 동시에_100개요청() throws InterruptedException {
        int threadCnt = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCnt);

        for (int i = 0; i < threadCnt; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        //100 - (1 * 100) = 0

        assertThat(stock.getQuantity()).isEqualTo(0);
    }

}