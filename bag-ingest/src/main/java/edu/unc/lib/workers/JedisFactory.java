package edu.unc.lib.workers;

import net.greghaines.jesque.Config;
import redis.clients.jedis.Jedis;

public class JedisFactory {

	/**
	 * Create a connection to Redis from the given Config.
	 * 
	 * @param config
	 *            the location of the Redis server
	 * @return a new connection
	 */
	public static Jedis createJedis(final Config config) {
		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}
		final Jedis jedis = new Jedis(config.getHost(), config.getPort(),
				config.getTimeout());
		if (config.getPassword() != null) {
			jedis.auth(config.getPassword());
		}
		jedis.select(config.getDatabase());
		return jedis;
	}
}
