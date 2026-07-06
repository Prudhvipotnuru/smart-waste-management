package com.prudhvi.swacch.config;

import java.io.IOException;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.prudhvi.swacch.model.House;
import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.repos.CollectorCredentialRepo;
import com.prudhvi.swacch.repos.HouseRepo;
import com.prudhvi.swacch.repos.UserRepo;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	private HouseRepo hRepo;
	
	private UserRepo uRepo;
	
	private CollectorCredentialRepo ccRepo;
	
	private PasswordEncoder passwordEncoder;
	
	public BatchConfig(HouseRepo hRepo, UserRepo uRepo, CollectorCredentialRepo ccRepo,
			PasswordEncoder passwordEncoder) {
		this.hRepo = hRepo;
		this.uRepo = uRepo;
		this.ccRepo = ccRepo;
		this.passwordEncoder = passwordEncoder;
	}

	// create reader
	@Bean
	@StepScope
	public FlatFileItemReader<House> reader(@Value("#{jobParameters['filePath']}") String filePath){
		FlatFileItemReader<House> reader =new FlatFileItemReader<House>(lineMapper());
		reader.setLinesToSkip(1);
		reader.setName("house-reader");
		reader.setStrict(true);
		reader.setResource(new FileSystemResource(filePath));
		
		return reader;
	}

	private LineMapper<House> lineMapper() {
		DefaultLineMapper<House> lineMapper=new DefaultLineMapper<House>();
		DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setNames("houseNumber","ownerName","address","ward");
		
		BeanWrapperFieldSetMapper<House> setMapper=new BeanWrapperFieldSetMapper<House>();
		setMapper.setTargetType(House.class);
		
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(setMapper);
		
		return lineMapper;
	}
	
	//processor
	@Bean
	public HouseProcessor processor() {
		return new HouseProcessor();
	}
	
	//writer
	@Bean
	public RepositoryItemWriter<House> dbWriter(){
		RepositoryItemWriter<House> itemWriter=new RepositoryItemWriter<House>(hRepo);
		itemWriter.setMethodName("save");
		return itemWriter;
	}
	
	@Bean
	public FlatFileItemWriter<House> errorWriter() {
	    FlatFileItemWriter<House> writer = new FlatFileItemWriter<House>(item->String.join(",",
	            item.getHouseNumber() != null ? item.getHouseNumber() : "",
	            item.getOwnerName() != null ? item.getOwnerName() : "",
	            item.getAddress() != null ? item.getAddress() : "",
	            item.getWard() != null ? item.getWard() : "",
	            String.valueOf(item.isError()),
	            item.getErrorDesc() != null ? item.getErrorDesc() : ""
	    ));
	    writer.setName("error-writer");
	    String errorFilePath = System.getProperty("user.dir") + "/uploads/error_records.csv";

	    writer.setResource(new FileSystemResource(errorFilePath));
	    writer.setHeaderCallback(w -> w.write("houseNumber,ownerName,address,ward,error,errorDescription"));

	    return writer;
	}
	
	@Bean
	public ClassifierCompositeItemWriter<House> compositeWriter(
	        RepositoryItemWriter<House> dbWriter,
	        FlatFileItemWriter<House> errorWriter) {

	    ClassifierCompositeItemWriter<House> writer = new ClassifierCompositeItemWriter<>();
	    writer.setClassifier(item -> item.isError() ? errorWriter : dbWriter);
	    return writer;
	}
	//step
	
	@Bean
	public Step housesStep(JobRepository jobRepository,FlatFileItemReader<House> reader,HouseProcessor processor,
			ClassifierCompositeItemWriter<House> writer,FlatFileItemWriter<House> errorWriter) throws IOException {
		return new StepBuilder(jobRepository)
			    .<House, House>chunk(10)
			    .reader(reader)
			    .processor(processor)
			    .writer(writer)
			    .stream(errorWriter)   // important: ensure writer is opened
			    .build();
	}
	//job
	
	@Bean
	public Job houseImportJob(JobRepository jobRepository, Step housesStep) {
	  return new JobBuilder("HouseImportJob",jobRepository)
	    .start(housesStep)
	    .build();
	}
	
	@Bean
	@StepScope
	public FlatFileItemReader<User> collectorsReader(@Value("#{jobParameters['filePath']}") String filePath){
		FlatFileItemReader<User> reader =new FlatFileItemReader<User>(collectorsLineMapper());
		reader.setLinesToSkip(1);
		reader.setName("collectors-reader");
		reader.setStrict(true);
		reader.setResource(new FileSystemResource(filePath));
		
		return reader;
	}
	
	private LineMapper<User> collectorsLineMapper() {
		DefaultLineMapper<User> lineMapper=new DefaultLineMapper<User>();
		DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setNames("name","role","phone","email","assigned_area");
		
		BeanWrapperFieldSetMapper<User> setMapper=new BeanWrapperFieldSetMapper<User>();
		setMapper.setTargetType(User.class);
		
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(setMapper);
		
		return lineMapper;
	}
	
	@Bean
	@StepScope
	public CollectorProcessor collectorProcessor(UserRepo uRepo,PasswordEncoder passwordEncoder,
			CollectorCredentialRepo ccRepo,@Value("#{jobParameters['jobExecutionId']}") Long jobExecutionId) {
		return new CollectorProcessor(uRepo, passwordEncoder, ccRepo, jobExecutionId);
	}
	
	@Bean
	public RepositoryItemWriter<User> dbCollectorWriter(){
		RepositoryItemWriter<User> itemWriter=new RepositoryItemWriter<User>(uRepo);
		itemWriter.setMethodName("save");
		return itemWriter;
	}
	
	@Bean
	public FlatFileItemWriter<User> collectorErrorWriter() {
	    FlatFileItemWriter<User> writer = new FlatFileItemWriter<User>(item->String.join(",",
	            item.getName() != null ? item.getName() : "",
	            item.getRole() != null ? item.getRole().name() : "",
	            item.getPhone() != null ? item.getPhone() : "",
	            item.getEmail() != null ? item.getEmail() : "",
	            item.getAssignedArea() != null ? item.getAssignedArea() : "",
	            String.valueOf(item.isError()),
	            item.getErrorDesc() != null ? item.getErrorDesc() : ""
	    ));
	    writer.setName("collector-error-writer");
	    String errorFilePath = System.getProperty("user.dir") + "/uploads/error_records.csv";

	    writer.setResource(new FileSystemResource(errorFilePath));
	    writer.setHeaderCallback(w -> w.write("name,role,phone,email,assigned_area,error,errorDescription"));

	    return writer;
	}
	
	@Bean
	public ClassifierCompositeItemWriter<User> collectorCompositeWriter(
	        RepositoryItemWriter<User> dbCollectorWriter,
	        FlatFileItemWriter<User> collectorErrorWriter) {

	    ClassifierCompositeItemWriter<User> writer = new ClassifierCompositeItemWriter<>();
	    writer.setClassifier(item -> item.isError() ? collectorErrorWriter : dbCollectorWriter);
	    return writer;
	}
	
	@Bean
	public Step collectorsStep(JobRepository jobRepository,FlatFileItemReader<User> reader,CollectorProcessor processor,
			ClassifierCompositeItemWriter<User> writer,FlatFileItemWriter<User> errorWriter) throws IOException {
		return new StepBuilder(jobRepository)
			    .<User, User>chunk(10)
			    .reader(reader)
			    .processor(processor)
			    .writer(writer)
			    .stream(errorWriter)   // important: ensure writer is opened
			    .build();
	}
	
	@Bean
	public Job collectorImportJob(JobRepository jobRepository, Step collectorsStep) {
	  return new JobBuilder("CollectorImportJob",jobRepository)
	    .start(collectorsStep)
	    .build();
	}
}

