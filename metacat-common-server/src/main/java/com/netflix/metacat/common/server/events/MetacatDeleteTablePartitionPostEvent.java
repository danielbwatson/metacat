/*
 *
 *  Copyright 2016 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.metacat.common.server.events;

import com.google.common.collect.Lists;
import com.netflix.metacat.common.MetacatRequestContext;
import com.netflix.metacat.common.QualifiedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MetacatDeleteTablePartitionPostEvent extends MetacatEvent {

    private final List<String> partitionIds;

    public MetacatDeleteTablePartitionPostEvent(
            @NotNull final QualifiedName name,
            @NotNull final MetacatRequestContext requestContext,
            @NotNull final List<String> partitionIds
    ) {
        super(name, requestContext);
        this.partitionIds = Lists.newArrayList(partitionIds);
    }

    /**
     * Get an unmodifiable view of the list of partition ids.
     *
     * @return The partition ids as an unmodifiable list that will throw an exception on attempt to modify
     */
    public List<String> getPartitionIds() {
        return Collections.unmodifiableList(this.partitionIds);
    }
}
