/**
 * 
 */
package test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
/**
 * @author Gautam Bhatnagar
 *
 */
public class Main {
	static DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
            new ProfileCredentialsProvider()));
    static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    static AmazonSNSClient snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());
    
    
    public static void main(String[] args) throws Exception
    {
    	snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    	System.out.println("testing main");
    	String email = "000gautam000@gmail.com";
    	String topicName = email.replaceAll("@|\\.","");
    	String phone = "19786219426";
    	System.out.println("Topic name: "+topicName);
    	CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
    	CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
    	//print TopicArn-+
    	String topicArn = createTopicResult.getTopicArn();
    	System.out.println("Topic arn: "+topicArn);
    	snsClient.setTopicAttributes(topicArn, "DisplayName", "stop slacking");
    	//get request id for CreateTopicRequest from SNS metadata		
    	System.out.println("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest));
    	
    	//subscribe to an SNS topic
    	SubscribeRequest subRequest = new SubscribeRequest(topicArn, "SMS", phone);
    	snsClient.subscribe(subRequest);
    	//get request id for SubscribeRequest from SNS metadata
    	System.out.println("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));
    	
    	loadTable("test", email, topicArn, phone);
    	getArn("test", email);
    }
    
    public static void loadTable(String tableName, String email, String arn, String ph)
    {
    	Table table = dynamoDB.getTable(tableName);
    	try{
    		 System.out.println("Adding data to " + tableName);
    		Item item = new Item()
    			.withPrimaryKey("email", email)
    			.withString("arn", arn)
    			.withString("ph", ph);
            table.putItem(item);
    	}
    	catch (Exception e){
            System.err.println("Failed to create item in " + tableName);
            System.err.println(e.getMessage());
    	}
    }
    
    public static void getArn(String tableName, String email)
    {
    	Table table = dynamoDB.getTable(tableName);

        Item item = table.getItem("email", // attribute name
                email, // attribute value
                "email, arn, ph",// projection expression
                null);

        System.out.println("GetItem: printing results...");
        System.out.println(item.getString("arn"));
        

    }

}
