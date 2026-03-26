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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.prudhvi.swacch.model.House;
import com.prudhvi.swacch.repos.HouseRepo;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	@Autowired
	private HouseRepo hRepo;
	
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
	public Step step(JobRepository jobRepository,FlatFileItemReader<House> reader,HouseProcessor processor, ClassifierCompositeItemWriter<House> writer,FlatFileItemWriter<House> errorWriter) throws IOException {
		return new StepBuilder(jobRepository)
			    .<House, House>chunk(10)
			    .reader(reader)
			    .processor(processor)
			    .writer(writer)
			    .stream(errorWriter)
			    .build();
	}
	//job
	
	@Bean
	public Job importUserJob(JobRepository jobRepository, Step step1) {
	  return new JobBuilder(jobRepository)
	    .start(step1)
	    .build();
	}
}

