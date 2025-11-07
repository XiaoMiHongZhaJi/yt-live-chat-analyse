package com.lwf.ytlivechatanalyse.auth.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

public class BufferedResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream buffer;
    private ServletOutputStream out;
    private PrintWriter writer;

    public BufferedResponseWrapper(HttpServletResponse resp) {
        super(resp);
        buffer = new ByteArrayOutputStream();
        out = new ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener listener) {}
            @Override
            public void write(int b) throws IOException { buffer.write(b); }
        };
        writer = new PrintWriter(new OutputStreamWriter(buffer, java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return out;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        super.flushBuffer();
        if (writer != null) writer.flush();
        if (out != null) out.flush();
    }

    public byte[] getBuffer() {
        try { flushBuffer(); } catch (IOException e) {}
        return buffer.toByteArray();
    }
}
