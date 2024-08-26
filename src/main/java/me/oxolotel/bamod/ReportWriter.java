/*
 *  Taken and modified from https://github.com/Aizistral-Studios/No-Chat-Reports/tree/f892d0c35ade02176357952ff74bd3cc00e7f35b
 */

package me.oxolotel.bamod;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.icu.text.SimpleDateFormat;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Allows to save chat reports locally. See {@link me.oxolotel.bamod.mixin.AbuseReportSenderMixin} for details.
 *
 * @author Aizistral
 */

public final class ReportWriter {
    private static final ObjectMapper MAPPER = ObjectMapper.create();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveReport(AbuseReportRequest report) {
        String string = MAPPER.writeValueAsString(report);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh.mm.ss");
        Path path = FabricLoader.getInstance().getGameDir().resolve("intercepted-reports/report-"
            + format.format(Date.from(report.report().createdTime())) + ".json");
        path.getParent().toFile().mkdirs();

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            GSON.toJson(GSON.fromJson(string, Object.class), writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}