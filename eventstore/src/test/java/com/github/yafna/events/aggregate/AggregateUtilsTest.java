package com.github.yafna.events.aggregate;

import com.github.yafna.events.rabbits.RabbitAdded;
import org.junit.Assert;
import org.junit.Test;

public class AggregateUtilsTest {
    @Test
    public void normal() {
        Assert.assertEquals("rabbit", AggregateUtils.resolveOrigin(new RabbitAdded("Bill", "Longear")));
    }

    @Test
    public void anonymous() {
        Assert.assertEquals("rabbit", AggregateUtils.resolveOrigin(new RabbitAdded("Bill", "Longear"){}));
    }
}