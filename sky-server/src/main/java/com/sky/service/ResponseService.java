package com.sky.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ResponseService {
    void export(HttpServletResponse response) throws IOException;
}
