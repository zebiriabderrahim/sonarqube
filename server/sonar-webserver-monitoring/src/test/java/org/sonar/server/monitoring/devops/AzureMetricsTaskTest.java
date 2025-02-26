/*
 * SonarQube
 * Copyright (C) 2009-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.monitoring.devops;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sonar.alm.client.azure.AzureDevOpsValidator;
import org.sonar.api.config.Configuration;
import org.sonar.db.DbClient;
import org.sonar.db.alm.setting.ALM;
import org.sonar.db.alm.setting.AlmSettingDao;
import org.sonar.db.alm.setting.AlmSettingDto;
import org.sonar.server.monitoring.ServerMonitoringMetrics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AzureMetricsTaskTest extends AbstractDevOpsMetricsTaskTest {

  private final ServerMonitoringMetrics metrics = mock(ServerMonitoringMetrics.class);
  private final AzureDevOpsValidator azureDevOpsValidator = mock(AzureDevOpsValidator.class);
  private final DbClient dbClient = mock(DbClient.class);
  private final Configuration config = mock(Configuration.class);

  private final AlmSettingDao almSettingsDao = mock(AlmSettingDao.class);

  private final AzureMetricsTask underTest = new AzureMetricsTask(metrics, azureDevOpsValidator, dbClient, config);

  @Before
  public void before() {
    when(dbClient.almSettingDao()).thenReturn(almSettingsDao);
  }

  @Test
  public void run_azureDevOpsValidatorDoesntThrowException_setGreenStatusInMetricsOnce() {
    List<AlmSettingDto> dtos = generateDtos(5, ALM.AZURE_DEVOPS);
    when(almSettingsDao.selectByAlm(any(), any())).thenReturn(dtos);

    underTest.run();

    verify(metrics, times(1)).setAzureStatusToGreen();
    verify(metrics, times(0)).setAzureStatusToRed();
  }

  @Test
  public void run_azureDevOpsValidatorDoesntThrowException_setRedStatusInMetricsOnce() {
    List<AlmSettingDto> dtos = generateDtos(5, ALM.AZURE_DEVOPS);
    when(almSettingsDao.selectByAlm(any(), any())).thenReturn(dtos);

    doThrow(new RuntimeException()).when(azureDevOpsValidator).validate(any());

    underTest.run();

    verify(metrics, times(0)).setAzureStatusToGreen();
    verify(metrics, times(1)).setAzureStatusToRed();
  }

  @Test
  public void run_azureIntegrationNotConfigured_setRedStatusInMetricsOnce() {
    when(almSettingsDao.selectByAlm(any(), any())).thenReturn(Collections.emptyList());

    underTest.run();

    verify(metrics, times(0)).setAzureStatusToGreen();
    verify(metrics, times(1)).setAzureStatusToRed();
  }
}
