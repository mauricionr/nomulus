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

package google.registry.tools;

import com.google.common.base.Optional;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import google.registry.model.registry.Registry;
import google.registry.model.registry.RegistryCursor;
import google.registry.model.registry.RegistryCursor.CursorType;
import google.registry.tools.params.DateTimeParameter;

import org.joda.time.DateTime;

import java.util.List;

/** Modifies {code RegistryCursor} timestamps used by locking rolling cursor tasks, like in RDE. */
@Parameters(separators = " =", commandDescription = "Modifies cursor timestamps used by LRC tasks")
final class UpdateCursorsCommand extends MutatingCommand {

  @Parameter(
      description = "TLDs on which to operate.",
      required = true)
  private List<String> tlds;

  @Parameter(
      names = "--type",
      description = "Which cursor to update.",
      required = true)
  private CursorType cursorType;

  @Parameter(
      names = "--timestamp",
      description = "The new timestamp to set.",
      validateWith = DateTimeParameter.class,
      required = true)
  private DateTime newTimestamp;

  @Override
  protected void init() throws Exception {
    for (String tld : tlds) {
      Registry registry = Registry.get(tld);
      Optional<DateTime> expectedTimestamp = RegistryCursor.load(registry, cursorType);
      stageEntityChange(
          expectedTimestamp.isPresent()
              ? RegistryCursor.create(registry, cursorType, expectedTimestamp.get())
              : null,
          RegistryCursor.create(registry, cursorType, newTimestamp));
    }
  }
}