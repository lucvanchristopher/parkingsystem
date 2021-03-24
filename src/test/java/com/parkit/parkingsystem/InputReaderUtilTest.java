package com.parkit.parkingsystem;

import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InputReaderUtilTest {
    private static final InputStream stdin = System.in;
    private static InputStream in = null;
    private static InputReaderUtil inputReaderUtil = null;

    private void provideInput(String data) {
        in = new ByteArrayInputStream((data + "\r\n").getBytes());
        System.setIn(in);
        inputReaderUtil = new InputReaderUtil();
    }

    @AfterAll
    private static void afterEachTest() throws Exception {
        System.setIn(stdin);
    }

    @Test
    public void shouldSuccessfullyReadSelection() {
        int selection = 3;
        provideInput("" + selection);

        assertEquals(selection, inputReaderUtil.readSelection());
    }

    @Test
    public void shouldReturnErrorReadSelection() {
        provideInput("a");

        assertEquals(-1, inputReaderUtil.readSelection());
    }

    @Test
    public void shouldSuccessfullyReadVehiculeNumber() throws Exception {
        String vehicleRegistrationNumber = "ABCDEFG";
        provideInput(vehicleRegistrationNumber);

        assertEquals(vehicleRegistrationNumber, inputReaderUtil.readVehicleRegistrationNumber());
    }

    @Test
    public void shouldRaiseExceptionReadVehiculeNumber() {
        provideInput("  ");

        assertThrows(IllegalArgumentException.class, () -> inputReaderUtil.readVehicleRegistrationNumber());
    }
}