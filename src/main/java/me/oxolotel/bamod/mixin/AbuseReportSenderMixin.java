/*
 *  Taken and modified from https://github.com/Aizistral-Studios/No-Chat-Reports/tree/f892d0c35ade02176357952ff74bd3cc00e7f35b
 */

package me.oxolotel.bamod.mixin;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.datafixers.util.Unit;
import me.oxolotel.bamod.ReportWriter;
import net.minecraft.client.session.report.AbuseReportSender;
import net.minecraft.client.session.report.AbuseReportType;
import net.minecraft.client.session.report.ReporterEnvironment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(AbuseReportSender.Impl.class)
public class AbuseReportSenderMixin{
    @Shadow
    @Final
    private ReporterEnvironment environment;

    /**
     * @reason Write report locally instead of trying to send it to Mojang.
     * Allows to intercept and examine contents of the report.
     * @author Aizistral
     */

    @Overwrite
    public CompletableFuture<Unit> send(UUID id, AbuseReportType type, AbuseReport report) {
        AbuseReportRequest abuseReportRequest = new AbuseReportRequest(1, id,
            report, this.environment.toClientInfo(), this.environment.toThirdPartyServerInfo(),
            this.environment.toRealmInfo(), type.getName());
        ReportWriter.saveReport(abuseReportRequest);
        return CompletableFuture.supplyAsync(() -> Unit.INSTANCE);
    }

}