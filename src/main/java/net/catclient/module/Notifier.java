package net.catclient.module;

/** Last notification, shown by the Dynamic Island module. */
public final class Notifier {
	public static String text = "";
	public static long time;

	private Notifier() {
	}

	public static void push(String message) {
		text = message;
		time = System.currentTimeMillis();
	}
}
