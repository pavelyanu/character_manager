package org.mff;
public class Main {
	public static void main(String[] args) {
		if (args.length > 0) {
			switch (args[0]) {
				case "server" -> Server.run(args);
				case "client" -> PlayerClient.run(args);
					default -> System.out.println("Unknown argument: " + args[0]);
			}
		} else {
			System.out.println("No arguments provided.");
			System.out.println("Use 'server' to run the server or 'client' to run the client");
		}
	}
}
