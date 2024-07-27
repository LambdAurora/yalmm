package yalmm.util;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FileUtils {
	public static boolean validateSha1(Path path, String checksum) {
		try (var is = Files.newInputStream(path)) {
			var digest = MessageDigest.getInstance("SHA-1");
			int n = 0;
			var buffer = new byte[8192];

			while (n != -1) {
				n = is.read(buffer);
				if (n > 0) {
					digest.update(buffer, 0, n);
				}
			}

			var builder = new StringBuilder();
			for (byte b : digest.digest()) {
				builder.append(String.format("%02x", b));
			}

			return checksum.contentEquals(builder);
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
