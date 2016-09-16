package de.subcentral.mig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class LogFilePlayground {

	public static void main(String[] args) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("E:\\Java\\git\\subcentral-core\\subcentral-mig\\subcentral-migration.log"));
		List<String> filteredLines = lines.stream().filter((String line) -> line.contains("Unknown column head")).collect(Collectors.toList());
		Files.write(Paths.get("E:\\Java\\SubCentral\\migration\\logs\\filtered.log"), filteredLines, StandardOpenOption.CREATE);
	}
}
