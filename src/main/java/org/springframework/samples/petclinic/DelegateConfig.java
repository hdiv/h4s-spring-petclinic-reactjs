package org.springframework.samples.petclinic;

import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.hdiv.config.annotation.ExclusionRegistry;
import org.hdiv.config.annotation.RuleRegistry;
import org.hdiv.config.annotation.ValidationConfigurer;
import org.hdiv.ee.config.SessionType;
import org.hdiv.ee.config.SingleCacheConfig;
import org.hdiv.ee.config.annotation.ExternalStateStorageConfigurer;
import org.hdiv.ee.session.cache.CacheType;
import org.hdiv.filter.ValidatorFilter;
import org.hdiv.listener.InitListener;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableEntityLinks;

import com.hdivsecurity.services.config.EnableHdiv4ServicesSecurityConfiguration;
import com.hdivsecurity.services.config.HdivServicesSecurityConfigurerAdapter;
import com.hdivsecurity.services.config.ServicesConfig.IdProtectionType;
import com.hdivsecurity.services.config.ServicesSecurityConfigBuilder;

@Configuration
@EnableHdiv4ServicesSecurityConfiguration
@EnableEntityLinks
public class DelegateConfig extends HdivServicesSecurityConfigurerAdapter {

	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		ValidatorFilter validatorFilter = new ValidatorFilter();
		registrationBean.setFilter(validatorFilter);
		registrationBean.setOrder(0);

		return registrationBean;
	}

	@Bean
	public InitListener initListener() {
		return new InitListener();
	}

	@Override
	public void configure(final ServicesSecurityConfigBuilder builder) {
		builder.confidentiality(false).sessionExpired().homePage("/");
		builder.showErrorPageOnEditableValidation(true);

		builder.createARegionPerControllerMapping(false);
		builder.reuseExistingPageInAjaxRequest(!Boolean.getBoolean("hdiv.dont.reuse"));

		if (Boolean.getBoolean("id.protection")) {
			builder.idProtection(IdProtectionType.PLAINTEXT_HID);
		}
		if (System.getProperty("session.type") != null) {
			SessionType type = SessionType.valueOf(System.getProperty("session.type"));
			builder.sessionType(type);
		}
		// builder.dashboardUser(null);
		builder.hypermediaSupport(false).csrfHeader(true);
	}

	@Override
	public void configureExternalStateStorage(final ExternalStateStorageConfigurer externalStateStorageConfigurer) {

		if (Boolean.getBoolean("redis.backend")) {
			SingleCacheConfig config = new SingleCacheConfig(CacheType.EXT_MEMORY);
			externalStateStorageConfigurer.redisExternalStateStore().host("127.0.0.1").port(6379);
			externalStateStorageConfigurer.cacheConfig(Arrays.asList(config));
		}
		if (Boolean.getBoolean("mongo.backend")) {
			SingleCacheConfig config = new SingleCacheConfig(CacheType.EXT_NO_SQL);
			externalStateStorageConfigurer.mongoExternalStateStore().host("127.0.0.1").port(27017);
			externalStateStorageConfigurer.cacheConfig(Arrays.asList(config));
		}
		if (Boolean.getBoolean("cassandra.backend")) {
			SingleCacheConfig config = new SingleCacheConfig(CacheType.EXT_NO_SQL);
			// ec2-54-84-143-245.compute-1.amazonaws.com
			externalStateStorageConfigurer.cassandraExternalStateStore().host("localhost").port(9042);
			externalStateStorageConfigurer.cacheConfig(Arrays.asList(config));
		}
		if (Boolean.getBoolean("postgres.backend")) {

			SingleCacheConfig config = new SingleCacheConfig(CacheType.EXT_DB);
			externalStateStorageConfigurer.databaseExternalStateStore().dataSource(externalStorageDataSource()).numberOfTables(4)
					.tablesSubjectName("Hdiv_Pages_");

			String hdivCacheSize = System.getProperty("hdiv.cache.size");
			String hdivBatchSize = System.getProperty("hdiv.batch.size");

			if (hdivCacheSize != null && hdivBatchSize != null) {
				SingleCacheConfig memory = new SingleCacheConfig(CacheType.SHARED);
				memory.setProperty(SingleCacheConfig.CACHE_SIZE, hdivCacheSize);
				memory.setProperty(SingleCacheConfig.BATCH_SIZE, hdivBatchSize);
				externalStateStorageConfigurer.cacheConfig(Arrays.asList(memory, config));
			}
			else {
				externalStateStorageConfigurer.cacheConfig(Arrays.asList(config));
			}
		}

		super.configureExternalStateStorage(externalStateStorageConfigurer);
	}

	public DataSource externalStorageDataSource() {
		final BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhosty/postgres");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres");
		return dataSource;
	}

	@Override
	public void addExclusions(final ExclusionRegistry registry) {
		registry.addUrlExclusions("/", "/metrics.*", "/scripts/.*", "/bootstrap/.*", "/images/.*", "/fonts/.*", "/angular-ui-router/.*",
				"/angular/.*", "/angular-cookies/.*", "/jquery/.*", "/css/.*", "/favicon.ico");
	}

	@Override
	public void addRules(final RuleRegistry registry) {
		registry.addRule("safeText").acceptedPattern("^[a-zA-Z0-9 :@.\\-_+#/]*$").rejectedPattern("(\\s|\\S)*(--)(\\s|\\S)*]");
		registry.addRule("numbers").acceptedPattern("^[1-9]\\d*$");
	}

	@Override
	public void configureEditableValidation(final ValidationConfigurer validationConfigurer) {
		validationConfigurer.addValidation("/.*").forParameters("amount").rules("numbers").disableDefaults();
		validationConfigurer.addValidation("/.*").rules("safeText").disableDefaults();
	}

}
