package me.beanes.acid.plugin.cloud;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import me.beanes.acid.plugin.cloud.pipeline.DefaultPipeline;

public class CloudInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        DefaultPipeline.addDefaultPipeline(pipeline);

        pipeline.addLast(new CloudRouter());
    }
}
