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
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class UpdateProfile
 */
public class UpdateProfile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DynamoDB dynamoDB;
    private SimpleDateFormat dateFormatter;
    private AmazonSNSClient snsClient;
    protected String tableName;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateProfile() {
        super();
        dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
                new ProfileCredentialsProvider()));
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());
        tableName = "test2";
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub


	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String topicName;
		String topicArn;
		//String email = request.getParameter("user");
		String phone = request.getParameter("user");
		String clientOrigin = request.getHeader("origin");
		System.out.println(clientOrigin);
		String changed = "false";
		
		Item record;
		if(phone== null)
		{
			System.err.println("Bad request.");
			return;
		}
		/*String phone = request.getParameter("phone");
		if(phone == null)
		{
			System.err.println("Bad request.");
			return;
		}
		topicName = email.replaceAll("@|\\.","");*/
		record = getRecord(tableName, phone);
		
		if(record == null)
		{
			topicArn = createTopic(phone);
			loadTable(tableName, phone, topicArn);
			changed = "true";
		}
		else
		{
			System.out.println("record exists");
			//if(record.getString("phone").equals(phone))
			//{
				System.out.println("nothing to do");
				
			//}
			/*topicArn = record.getString("arn");
			//Delete existing topic to change phone number
			DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(topicArn);
			snsClient.deleteTopic(deleteTopicRequest);
			//get request id for DeleteTopicRequest from SNS metadata
			System.out.println("DeleteTopicRequest - " + snsClient.getCachedResponseMetadata(deleteTopicRequest));
			topicArn = createTopic(topicName, phone);
			loadTable(tableName, email, topicArn, phone);*/
		}
		
        if(clientOrigin.equals("chrome-extension://jnplobmicjmincpjonhajdhcaeimbakn"))
        {
        	response.setContentType("text/html");
        	response.setHeader("Cache-control", "no-cache, no-store");
        	response.setHeader("Pragma", "no-cache");
        	response.setHeader("Expires", "-1");
        	response.setHeader("Access-Control-Allow-Origin", clientOrigin);
        	response.setHeader("Access-Control-Allow-Methods", "POST");
        	response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        	response.setHeader("Access-Control-Max-Age", "86400");
        }
        else
        {
        	System.err.println("Unauthorized origin.");
        
        }
        response.getWriter().println(changed);
        //request.setAttribute("changed", changed); // This will be available as ${message}
        //request.getRequestDispatcher("/WEB-INF/response.jsp").forward(request, response);
	}
	
    protected void loadTable(String tableName, String phone, String arn)
    {
    	Table table = dynamoDB.getTable(tableName);
    	try{
    		 System.out.println("Adding data to " + tableName);
    		Item item = new Item()
    			.withPrimaryKey("phone", phone)
    			.withString("arn", arn);
            table.putItem(item);
    	}
    	catch (Exception e){
            System.err.println("Failed to create item in " + tableName);
            System.err.println(e.getMessage());
    	}
    }
    
    protected Item getRecord(String tableName, String phone)
    {
    	Table table = dynamoDB.getTable(tableName);

        Item item = table.getItem("phone", // attribute name
                phone, // attribute value
                "phone, arn",// projection expression
                null);

        
        //System.out.println("GetItem: printing results...");
        //System.out.println(item.getString("arn"));
        return item;

    }
    
    protected String createTopic(String phone)
    {
    	System.out.println("Topic name: "+phone);
    	CreateTopicRequest createTopicRequest = new CreateTopicRequest(phone);
    	CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
    	//print TopicArn-+
    	String topicArn = createTopicResult.getTopicArn();
    	System.out.println("Topic arn: "+topicArn);
    	snsClient.setTopicAttributes(topicArn, "DisplayName", "nag");
    	//get request id for CreateTopicRequest from SNS metadata		
    	System.out.println("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest));
    	
    	//subscribe to an SNS topic
    	SubscribeRequest subRequest = new SubscribeRequest(topicArn, "SMS", phone);
    	snsClient.subscribe(subRequest);
    	//get request id for SubscribeRequest from SNS metadata
    	System.out.println("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));
    	return topicArn;
    }
    
    
}
