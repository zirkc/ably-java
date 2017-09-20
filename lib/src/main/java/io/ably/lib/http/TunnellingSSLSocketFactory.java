package io.ably.lib.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import io.ably.lib.types.ProxyOptions;

public class TunnellingSSLSocketFactory extends SSLSocketFactory {

	TunnellingSSLSocketFactory(ProxyOptions proxyOptions, HttpAuth proxyAuth) {
		this.proxyOptions = proxyOptions;
		this.proxyAuth = proxyAuth;
	}

	@Override
	public Socket createSocket(Socket arg0, String host, int port, boolean autoClose) throws IOException {
		/* FIXME: discard given socket */

	   /*
         * Set up a socket to do tunneling through the proxy.
         * Start it off as a regular socket, then layer SSL
         * over the top of it.
         */
        Socket tunnel = new Socket(proxyOptions.host, proxyOptions.port);
        doTunnelHandshake(tunnel, host, port);

        /*
         * Ok, let's overlay the tunnel socket with SSL.
         */
        SSLSocket socket = (SSLSocket)socketFactory.createSocket(tunnel, host, port, true);

        /*
         * register a callback for handshaking completion event
         */
        socket.addHandshakeCompletedListener(
            new HandshakeCompletedListener() {
                public void handshakeCompleted(HandshakeCompletedEvent event) {
                    System.out.println("Handshake finished!");
                    System.out.println(
                        "\t CipherSuite:" + event.getCipherSuite());
                    System.out.println(
                        "\t SessionId " + event.getSession());
                    System.out.println(
                        "\t PeerHost " + event.getSession().getPeerHost());
                }
            }
        );

		return socket;
	}

    /*
     * Tell our tunnel where we want to CONNECT, and look for the
     * right reply.  Throw IOException if anything goes wrong.
     */
    private int doTunnelHandshake(Socket tunnel, String host, int port)
    throws IOException
    {
        OutputStream out = tunnel.getOutputStream();
        String address = host + ":" + port;
        String msg = "CONNECT " + address + " HTTP/1.0\r\n"
                     + "Host: " + address + "\r\n"
                     + "\r\n\r\n";
        byte b[];
        try {
            /*
             * We really do want ASCII7 -- the http protocol doesn't change
             * with locale.
             */
            b = msg.getBytes("ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            /*
             * If ASCII7 isn't there, something serious is wrong, but
             * Paranoia Is Good (tm)
             */
            b = msg.getBytes();
        }
        out.write(b);
        out.flush();

        /*
         * We need to store the reply so we can create a detailed
         * error message to the user.
         */
        byte            reply[] = new byte[200];
        int             replyLen = 0;
        int             newlinesSeen = 0;
        boolean         headerDone = false;     /* Done on first newline */

        InputStream     in = tunnel.getInputStream();
        boolean         error = false;

        while (newlinesSeen < 2) {
            int i = in.read();
            if (i < 0) {
                throw new IOException("Unexpected EOF from proxy");
            }
            if (i == '\n') {
                headerDone = true;
                ++newlinesSeen;
            } else if (i != '\r') {
                newlinesSeen = 0;
                if (!headerDone && replyLen < reply.length) {
                    reply[replyLen++] = (byte) i;
                }
            }
        }

        /*
         * Converting the byte array to a string is slightly wasteful
         * in the case where the connection was successful, but it's
         * insignificant compared to the network overhead.
         */
        String replyStr;
        try {
            replyStr = new String(reply, 0, replyLen, "ASCII7");
        } catch (UnsupportedEncodingException ignored) {
            replyStr = new String(reply, 0, replyLen);
        }

        /* We asked for HTTP/1.0, so we should get that back */
        if (replyStr.startsWith("HTTP/1.0 200")) {
            /* tunneling Handshake was successful! */
        	return 200;
        }

        /* If we get 1.1 then its ok */
        if (replyStr.startsWith("HTTP/1.1 200")) {
            /* tunneling Handshake was successful! */
        	return 200;
        }

        throw new IOException("Unable to tunnel through "
                    + proxyOptions.host + ":" + proxyOptions.port
                    + ".  Proxy returns \"" + replyStr + "\"");

    }

    @Override
	public String[] getDefaultCipherSuites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedCipherSuites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
		return null;
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		return null;
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		return null;
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
		return null;
	}

	private final ProxyOptions proxyOptions;
	private final HttpAuth proxyAuth;
	private static final SSLSocketFactory socketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
}
