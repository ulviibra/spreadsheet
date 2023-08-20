package com.orkes.spreadsheet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@AutoConfigureMockMvc
public class SpreadsheetApplicationTest {
    private static final String VALUE = "value";
    private static final String ENDPOINT = "/api/spreadsheet/cell/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testBasicSetAndGetCellValue() throws Exception {
        performSetAndExpectOK("A1", "13");
        performGetAndExpectOK("A1", "13");
    }

    @Test
    public void testSetAndEvaluateFormula() throws Exception {
        performSetAndExpectOK("A1", "13");
        performSetAndExpectOK("A2", "14");
        performSetAndExpectOK("A3", "A1+A2");
        performSetAndExpectOK("A4", "A1+A2+A3");
        performGetAndExpectOK("A3", "27");
        performGetAndExpectOK("A4", "54");
    }
    @Test
    public void testSubtractionAndMultiplicationFormula() throws Exception {
        performSetAndExpectOK("A1", "10");
        performSetAndExpectOK("A2", "5");
        performSetAndExpectOK("A3", "A1-A2");
        performSetAndExpectOK("A4", "A1*A2");
        performGetAndExpectOK("A3", "5");
        performGetAndExpectOK("A4", "50");
    }
    @Test
    public void testCellAndIntegerFormula() throws Exception {
        performSetAndExpectOK("A1", "20");
        performSetAndExpectOK("A2", "A1+5");
        performSetAndExpectOK("A3", "A2*2-A1");
        performGetAndExpectOK("A2", "25");
        performGetAndExpectOK("A3", "30");
    }

    @Test
    public void testCellEqualsAnotherCell() throws Exception {
        performSetAndExpectOK("A1", "-20");
        performSetAndExpectOK("A2", "A1");
        performGetAndExpectOK("A2", "-20");
    }
    @Test
    public void testDivision() throws Exception {
        performSetAndExpectOK("A1", "20");
        performSetAndExpectOK("A2", "4");
        performSetAndExpectOK("A3", "A1/A2");
        performGetAndExpectOK("A3", "5");

        performSetAndExpectOK("A4", "20");
        performSetAndExpectOK("A5", "0");
        performSetAndExpectBadRequest("A6", "A4/A5");
    }
    @Test
    public void testOperatorPrecedence() throws Exception {
        performSetAndExpectOK("A1", "10");
        performSetAndExpectOK("A2", "5");
        performSetAndExpectOK("A3", "2");
        performSetAndExpectOK("A4", "15");
        performSetAndExpectOK("A5", "A1*A2+A3-A4/A2");
        performGetAndExpectOK("A5", "49");

        performSetAndExpectOK("B1", "6");
        performSetAndExpectOK("B2", "2");
        performSetAndExpectOK("B3", "4");
        performSetAndExpectOK("B4", "B1*B2/B3");
        performSetAndExpectOK("B5", "B1-B2+B4");
        performGetAndExpectOK("B5", "7");

        performSetAndExpectOK("C1", "8");
        performSetAndExpectOK("C2", "2");
        performSetAndExpectOK("C3", "3");
        performSetAndExpectOK("C4", "C1/C2*C3");
        performSetAndExpectOK("C5", "C1+C2-C4/C3");
        performGetAndExpectOK("C5", "6");

        performSetAndExpectOK("D1", "10");
        performSetAndExpectOK("D2", "3");
        performSetAndExpectOK("D3", "2");
        performSetAndExpectOK("D4", "D1-D2/D3");
        performSetAndExpectOK("D5", "D1*D2+D4");
        performGetAndExpectOK("D5", "39");
    }
    @Test
    public void testComplexFormulaWithNegativeValues() throws Exception {
        performSetAndExpectOK("A1", "-5");
        performSetAndExpectOK("A2", "3");
        performSetAndExpectOK("A3", "15");
        performSetAndExpectOK("A4", "A1*A2+A3/A1-A2");
        performGetAndExpectOK("A4", "-21");
    }
    @Test
    public void testFormulaWithSpaces() throws Exception {
        performSetAndExpectOK("A1", "-5");
        performSetAndExpectOK("A2", "3");
        performSetAndExpectOK("A3", "   A1   +   A2   ");
        performGetAndExpectOK("A3", "-2");
    }
    @Test
    public void testInvalidCellReference() throws Exception {
        performSetAndExpectOK("A1", "13");
        performSetAndExpectBadRequest("A2", "A1+Z5");
    }

    @Test
    public void testGetNonExistentCell() throws Exception {
        performGetAndExpectBadRequest("Z1");
    }

    @Test
    public void testInvalidFormula() throws Exception {
        performSetAndExpectBadRequest("A1", "AAAAA");
        performSetAndExpectBadRequest("A1", "");
        performSetAndExpectBadRequest("A1", "+++++------//****");
        performSetAndExpectBadRequest("A1", "++");
        performSetAndExpectBadRequest("A1", "--");
        performSetAndExpectBadRequest("A1", "++A2");
        performSetAndExpectBadRequest("A1", "A2+");
        performSetAndExpectBadRequest("A1", "A2++");
        performSetAndExpectBadRequest("A1", "*");
        performSetAndExpectBadRequest("A1", "/");
        performSetAndExpectBadRequest("A1", "*A2");
        performSetAndExpectBadRequest("A1", "A2*");
        performSetAndExpectBadRequest("A1", "A2**");
    }

    @Test
    public void testEmptyCellIdSetAndGet() throws Exception {
        performGetAndExpectBadRequest("");
        performSetAndExpectBadRequest("", "");
    }

    @Test
    public void testConcurrentSetAndGet() throws Exception {
        int numThreads = 10;
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                Future<?> future = executor.submit(() -> {
                    try {
                        performSetAndExpectOK("A1", String.valueOf(threadId));
                    } catch (Exception e) {
                        fail(e);
                    }
                });
                future.get();
            }

            for (int i = 0; i < numThreads; i++) {
                Future<?> future = executor.submit(() -> {
                    try {
                        performSetAndExpectOK("A1", "13");
                        performGetAndExpectOK("A1", "13");
                    } catch (Exception e) {
                        fail(e);
                    }
                });
                future.get();
            }
            executor.shutdown();
        }
    }

    private void performSetAndExpectOK(String cellId, String value) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT, cellId)
                        .param(VALUE, value))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    private void performGetAndExpectOK(String cellId, String expectedValue) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT, cellId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(expectedValue));
    }

    private void performSetAndExpectBadRequest(String cellId, String value) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT, cellId)
                        .param(VALUE, value))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    private void performGetAndExpectBadRequest(String cellId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT, cellId))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
}
