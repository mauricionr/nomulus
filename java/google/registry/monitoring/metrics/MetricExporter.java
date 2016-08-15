// Copyright 2016 The Domain Registry Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.monitoring.metrics;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import java.util.concurrent.BlockingQueue;

/**
 * Background service to asynchronously push bundles of {@link MetricPoint} instances to a {@link
 * MetricWriter}.
 */
class MetricExporter extends AbstractExecutionThreadService {

  private final BlockingQueue<Optional<ImmutableList<MetricPoint<?>>>> writeQueue;
  private final MetricWriter writer;

  MetricExporter(
      BlockingQueue<Optional<ImmutableList<MetricPoint<?>>>> writeQueue, MetricWriter writer) {
    this.writeQueue = writeQueue;
    this.writer = writer;
  }

  @Override
  protected void run() throws Exception {
    while (isRunning()) {
      Optional<ImmutableList<MetricPoint<?>>> batch = writeQueue.take();
      if (batch.isPresent()) {
        for (MetricPoint<?> point : batch.get()) {
          writer.write(point);
        }
        writer.flush();
      } else {
        // An absent optional indicates that the Reporter wants this service to shut down.
        return;
      }
    }
  }
}