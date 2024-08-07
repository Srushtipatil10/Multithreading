package org.example;
import java.io.IOException;
import java.io.*;
public class Main {
    public static void main(String[] args) throws IOException {
            try (Read rd = new Read()) {
                rd.readData();
            }
        }
    }

