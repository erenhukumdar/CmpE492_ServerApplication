package edu.boun.gglass.ui;

public interface uiCallbacks {
	void onHttpServerStart();
	void onHttpServerStop();

    void onMdnsServerStart();
    void onMdnsServerStop();
    
    void onImageReceived(String imagePath, String Ip);
}
