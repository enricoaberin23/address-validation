/* 
 * COSC 4351 Fundamentals of Software Engineering
 * Authors: Enrico Aberin, Eduardo Hernandez
 * Description: Address validation module for e-commerce user. 
 * Validates the address provided by user using USPS Web Tools API service.
 *
 */



package AddressValidation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


@SuppressWarnings("serial")

//Address validation user interface (UI)
public class UI extends JFrame implements ActionListener
{
		//UI components
		JTextField streetField, cityField, stateField, aptSuiteField, zipField;
	    JButton validateButton, clearButton;
	    JComboBox stateList;
	    JLabel textDisplay;
	    
	    //UI constructor
	    public UI() 
	    {
	    	super("Address Validation");
	        setLayout(new FlowLayout());
	        
	        
	        JPanel leftPanel = new JPanel(new GridLayout(7, 1, 20, 10));
	        JPanel rightPanel = new JPanel(new BorderLayout());
	        rightPanel.setPreferredSize(new Dimension(300,200));
	        
	        leftPanel.add(new JLabel("* indicates required field"));
	        leftPanel.add(new JLabel(""));
	        leftPanel.add(new JLabel("Apt/Suite/Other"));
	        aptSuiteField = new JTextField(20);
	        leftPanel.add(aptSuiteField);
	        
	        //Allows only numeric input for this field
	        aptSuiteField.addKeyListener(new KeyAdapter() {
	            public void keyTyped(KeyEvent e) 
	            { 
	            	try
	            	{
	            		int aptSuite = Integer.parseInt(aptSuiteField.getText());
	            	}
	            	catch (NumberFormatException a)
	            	{
	            		aptSuiteField.setText("");
	            	}
	            }  
	        });
	        
	        leftPanel.add(new JLabel("Street Address *"));
	        streetField = new JTextField(20);
	        leftPanel.add(streetField);
	        
	        leftPanel.add(new JLabel("City *"));
	        cityField = new JTextField(20);
	        leftPanel.add(cityField);

	        
	        
	        leftPanel.add(new JLabel("State *"));
	        String[] stateStrings = 
	        {
	        		"Select", 
	        		"Alabama",
	                "Alaska",
	                "American Samoa",
	                "Arizona",
	                "Arkansas",
	                "California",
	                "Colorado",
	                "Connecticut",
	                "Delaware",
	                "District of Columbia",
	                "Federated Stated of Micronesia",
	                "Florida",
	                "Georgia",
	                "Guam",
	                "Hawaii",
	                "Idaho",
	                "Illinois",
	                "Indiana",
	                "Iowa",
	                "Kansas",
	                "Kentucky",
	                "Louisiana",
	                "Maine",
	                "Marshall Islands",
	                "Maryland",
	                "Massachusetts",
	                "Michigan",
	                "Minnesota",
	                "Mississippi",
	                "Missouri",
	                "Montana",
	                "Nebraska",
	                "Nevada",
	                "New Hampshire",
	                "New Jersey",
	                "New Mexico",
	                "New York",
	                "North Carolina",
	                "North Dakota",
	                "Northern Mariana Islands",
	                "Ohio",
	                "Oklahoma",
	                "Oregon",
	                "Palau",
	                "Pennsylvania",
	                "Puerto Rico",
	                "Rhode Island",
	                "South Carolina",
	                "South Dakota",
	                "Tennessee",
	                "Texas",
	                "Utah",
	                "Vermont",
	                "Virgin Islands",
	                "Virginia",
	                "Washington",
	                "West Virginia",
	                "Wisconsin",
	                "Wyoming",
	                "Armed Forces Americas",
	                "Armed Forces Africa",
	                "Armed Forces Canada",
	                "Armed Forces Europe",
	                "Armed Forces Middle East",
	                "Armed Forces Pacific"
	        };
	  	    stateList = new JComboBox(stateStrings);
	  	    stateList.setSelectedIndex(0);
	        leftPanel.add(stateList);
	        
	        leftPanel.add(new JLabel("ZIP Code"));
	        zipField = new JTextField(20);
	        leftPanel.add(zipField);
	        
	        //Allows only numeric input for this field
	        //Allows maximum of 5 characters for this field 
	        zipField.addKeyListener(new KeyAdapter() {
	            public void keyTyped(KeyEvent e) 
	            { 
	                try 
	                {
	                	int zip = Integer.parseInt(zipField.getText());
	                	if (zipField.getText().length() >= 5)
	                	{
	                		e.consume();
	                	}
	                }
	                catch (NumberFormatException a)
	                {
	                	zipField.setText("");
	                }
	            }  
	        });
	        
	        validateButton = new JButton("Validate");
	        leftPanel.add(validateButton);
	        validateButton.addActionListener(this);
	        
	        
	        clearButton = new JButton("Clear");
	        leftPanel.add(clearButton);
	        clearButton.addActionListener(this);
	        
	        textDisplay = new JLabel();
	        textDisplay.setHorizontalAlignment(JLabel.CENTER);
	        
	        rightPanel.add(textDisplay);
	        textDisplay.setText("NO ADDRESS SET.");
	        textDisplay.setForeground(Color.red);
	        textDisplay.setFont(new Font("Dialog", Font.ITALIC, 16));
	        add(leftPanel);
	        add(rightPanel);
	        
	        setDefaultCloseOperation(EXIT_ON_CLOSE);
	        setResizable(false);
	        pack();
	        setVisible(true);
	   
	    }
	    
	    //Calls event when the user clicks a button
	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
	    	if (e.getSource() == validateButton)
	    	{
	    		//Reads user entry from each field
	    		String aptSuiteFieldText = aptSuiteField.getText();
	    		String streetFieldText = streetField.getText();
	    		String cityFieldText = cityField.getText();
	    		String zipFieldText = zipField.getText();
	    		
	    		//Reads selected item from list of states
	    		//Converts selected state names to two-letter state abbreviations as required by USPS
	    		String stateListText = stateList.getSelectedItem().toString();
	    		String stateAbbreviationText = convertToStateAbbreviations(stateListText);
	    		
	    		
	    		validateAddress(aptSuiteFieldText, streetFieldText, cityFieldText, stateAbbreviationText, zipFieldText);
	    		
	    	}
	    	else
	    	{
	    		clearAddress();
	    	}
	    }   
	    
	    //Schedules a job for the event dispatch thread
	    //Creates and shows UI
	    public static void main(String args[])
	    {
	    	javax.swing.SwingUtilities.invokeLater(new Runnable() 
	    	{
	            public void run() 
	            {
	                new UI();
	            }
	        });
	    }
	    
	    //Gets user entries and builds them into an XML request to USPS Shipping Web Tools server 
	    public void validateAddress(String a, String b, String c, String d, String e)
	    {
	    	String aptSuiteFieldText = a;
	    	String streetFieldText = b;
	    	String cityFieldText = c;
	    	String stateListText = d;
	    	String zipFieldText = e;
	    	
	    	
	    	
	    	if (streetField.getText().equals("") || cityField.getText().equals("") || stateListText == "Select")
    		{
    			textDisplay.setText("MISSING REQUIRED FIELD.");
    			textDisplay.setForeground(Color.red);
    	        textDisplay.setFont(new Font("Dialog", Font.ITALIC, 16));
    			
    		}
    		else
    		{
    			try 
        		{
    				//Follows XML transaction format
    				//Requires USPS user ID
    				String userID = "048NA0003218";
        			String url = "http://production.shippingapis.com/ShippingAPI.dll?API=Verify&XML=<AddressValidateRequest USERID=\""+ userID + "\">" + 
        					"<Address>" + 
        					"<Address1>" + aptSuiteFieldText + "</Address1>" + 
        					"<Address2>" + streetFieldText + "</Address2>" + 
        					"<City>" + cityFieldText + "</City>" + 
        					"<State>" + stateListText + "</State>" + 
        					"<Zip5>" + zipFieldText + "</Zip5>" + 
        					"<Zip4></Zip4>" +
        					"</Address>" + 
        					"</AddressValidateRequest>";
        			//Encodes %20 for spaces
        			url = url.replace(" ", "%20");
        			URL obj = new URL(url);
        			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        			String inputLine;
        			StringBuffer response = new StringBuffer();
        			while ((inputLine = in.readLine()) != null) 
        			{
        				response.append(inputLine);
        			}
        			in.close();
        				
        			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(response.toString())));
        			NodeList errNodes = doc.getElementsByTagName("AddressValidateResponse");
        	        	if (errNodes.getLength() > 0) 
        	        	{
        	        		Element err = (Element)errNodes.item(0);
        	        			
        	        		//Parses XML response from USPS Shipping Web Tools server and prints to text display
        	        		
        	        		if (aptSuiteField.getText().equals(""))
        	        		{
        	        			try 
                     	        {
        	        				//Displays error if ReturnText tag is not empty which indicates multiple addresses was found
                     	        	if (err.getElementsByTagName("ReturnText").item(0).getTextContent() != "")
                     	        	{
                     	        		textDisplay.setText("<html>ADDRESS FOUND, " +
                     	        	    "<br>" + "BUT MORE INFORMATION " +
                     	        	    "<br>" + "IS NEEEDED (SUCH AS " +
                     	        		"<br>" + "APT, SUITE OR BOX #)" + 
                     	        		"<br>" + "TO MATCH TO A " +
                     	        		"<br>" + "SPECIFIC ADDRESS." + "</html>");
                     	        		
                     	        		textDisplay.setForeground(Color.red);
                                        textDisplay.setFont(new Font("Dialog", Font.ITALIC, 16));
                     	        	}	
                     	        }
        	        			//Prints parsed XML response without unit number (apartment, suite or other) to display
                     	        catch (NullPointerException f)
                     	        {
                     	        	textDisplay.setText("<html> ADDRESS FOUND. " + "<br>" +
                                    "<br>" + err.getElementsByTagName("Address2").item(0).getTextContent() + 
                                    "<br>" + err.getElementsByTagName("City").item(0).getTextContent() + 
                                    "<br>" + err.getElementsByTagName("State").item(0).getTextContent() + 
                                    "<br>" + err.getElementsByTagName("Zip5").item(0).getTextContent() + 
                                    " - "  + err.getElementsByTagName("Zip4").item(0).getTextContent() + "</html>");
                     	        	
                                    textDisplay.setForeground(Color.black);
                                    textDisplay.setFont(new Font("Dialog", Font.PLAIN, 16));
                     	        }
                	      
        	        		}
        	        		//Prints parsed XML response with unit number (apartment, suite or other) to display
        	        		else
        	        		{
        	        			textDisplay.setText("<html> ADDRESS FOUND. " + "<br>" +
        	        			"<br>" + err.getElementsByTagName("Address2").item(0).getTextContent() + 
        	                	"<br>" + err.getElementsByTagName("Address1").item(0).getTextContent() + 
        	                	"<br>" + err.getElementsByTagName("City").item(0).getTextContent() + 
        	                	"<br>" + err.getElementsByTagName("State").item(0).getTextContent() + 
        	                	"<br>" + err.getElementsByTagName("Zip5").item(0).getTextContent() + 
        	                	" - "  + err.getElementsByTagName("Zip4").item(0).getTextContent() + "</html>");
        	        			
        	                	textDisplay.setForeground(Color.black);
        	                	textDisplay.setFont(new Font("Dialog", Font.PLAIN, 16));
        	        		}
        	        		
        	        	
        	        	} 
        	        	else 
        	        	{ 
        	        		
        	        	}
        		} 
    			
    			//Prints message if address was not found
        		catch (Exception f) 
        		{
        			textDisplay.setText("ADDRESS NOT FOUND.");
        			textDisplay.setForeground(Color.red);
        			textDisplay.setFont(new Font("Dialog", Font.ITALIC, 16));
        		}
    			
   
    			
    		}
	    }
	    
	    //Clears user fields
	    public void clearAddress()
	    {
	    	aptSuiteField.setText("");
    		streetField.setText("");
    		cityField.setText("");
    		stateList.setSelectedIndex(0);
    		zipField.setText("");
    		textDisplay.setText("NO ADDRESS SET.");
    		textDisplay.setForeground(Color.red);
			textDisplay.setFont(new Font("Dialog", Font.ITALIC, 16));
	    }
	    
	    //Converts selected state names to two-letter state abbreviations as required by USPS
	    public String convertToStateAbbreviations(String a)
	    {
	    	String stateAbbreviation = a;
	    	
	    	switch (stateAbbreviation)
	    	{
	    		case "Alabama":
	    			stateAbbreviation = "AL";
	    			break;
	    		case "Alaska":
	    			stateAbbreviation = "AK";
	    			break;
	    		case "American Samoa":
	    			stateAbbreviation = "AS";
	    			break;
	    		case "Arizona":
	    			stateAbbreviation = "AZ";
	    			break;
	    		case "Arkansas":
	    			stateAbbreviation = "AR";
	    			break;
	    		case "California":
	    			stateAbbreviation = "CA";
	    			break;
	    		case "Colorado":
	    			stateAbbreviation = "CO";
	    			break;
	    		case "Connecticut":
	    			stateAbbreviation = "CT";
	    			break;
	    		case "Delaware":
	    			stateAbbreviation = "DE";
	    			break;
	    		case "District of Columbia":
	    			stateAbbreviation = "DC";
	    			break;
	    		case "Federated Stated of Micronesia":
	    			stateAbbreviation = "FM";
	    			break;
	    		case "Florida":
	    			stateAbbreviation = "FL";
	    			break;
	    		case "Georgia":
	    			stateAbbreviation = "GA";
	    			break;
	    		case "Guam":
	    			stateAbbreviation = "GU";
	    			break;
	    		case "Hawaii":
	    			stateAbbreviation = "HI";
	    			break;
	    		case "Idaho":
	    			stateAbbreviation = "ID";
	    			break;
	    		case "Illinois":
	    			stateAbbreviation = "IL";
	    			break;
	    		case "Indiana":
	    			stateAbbreviation = "IN";
	    			break;
	    		case "Iowa":
	    			stateAbbreviation = "IA";
	    			break;
	    		case "Kansas":
	    			stateAbbreviation = "KS";
	    			break;
	    		case "Kentucky":
	    			stateAbbreviation = "KY";
	    			break;
	    		case "Louisiana":
	    			stateAbbreviation = "LA";
	    			break;
	    		case "Maine":
	    			stateAbbreviation = "ME";
	    			break;
	    		case "Marshall Islands":
	    			stateAbbreviation = "MH";
	    			break;
	    		case "Maryland":
	    			stateAbbreviation = "MD";
	    			break;
	    		case"Massachusetts":
	    			stateAbbreviation = "MA";
	    			break;
	    		case "Michigan":
	    			stateAbbreviation = "MI";
	    			break;
	    		case "Minnesota":
	    			stateAbbreviation = "MN";
	    			break;
	    		case "Mississippi":
	    			stateAbbreviation = "MS";
	    			break;
	    		case "Missouri":
	    			stateAbbreviation = "MO";
	    			break;
	    		case "Montana":
	    			stateAbbreviation = "MT";
	    			break;
	    		case "Nebraska":
	    			stateAbbreviation = "NE";
	    			break;
	    		case "Nevada":
	    			stateAbbreviation = "NV";
	    			break;
	    		case "New Hampshire":
	    			stateAbbreviation = "NH";
	    			break;
	    		case "New Jersey":
	    			stateAbbreviation = "NJ";
	    			break;
	    		case "New Mexico":
	    			stateAbbreviation = "NM";
	    			break;
	    		case "New York":
	    			stateAbbreviation = "NY";
	    			break;
	    		case "North Carolina":
	    			stateAbbreviation = "NC";
	    			break;
	    		case "North Dakota":
	    			stateAbbreviation = "ND";
	    			break;
	    		case "Northern Mariana Islands":
	    			stateAbbreviation = "MP";
	    			break;
	    		case "Ohio":
	    			stateAbbreviation = "OH";
	    			break;
	    		case "Oklahoma":
	    			stateAbbreviation = "OK";
	    			break;
	    		case "Oregon":
	    			stateAbbreviation = "OR";
	    			break;
	    		case "Palau":
	    			stateAbbreviation = "PW";
	    			break;
	    		case "Pennsylvania":
	    			stateAbbreviation = "PA";
	    			break;
	    		case "Puerto Rico":
	    			stateAbbreviation = "PR";
	    			break;
	    		case "Rhode Island":
	    			stateAbbreviation = "RI";
	    			break;
	    		case "South Carolina":
	    			stateAbbreviation = "SC";
	    			break;
	    		case "South Dakota":
	    			stateAbbreviation = "SD";
	    			break;
	    		case "Tennessee":
	    			stateAbbreviation = "TN";
	    			break;
	    		case "Texas":
	    			stateAbbreviation = "TX";
	    			break;
	    		case "Utah":
	    			stateAbbreviation = "UT";
	    			break;
	    		case "Vermont":
	    			stateAbbreviation = "VT";
	    			break;
	    		case "Virgin Islands":
	    			stateAbbreviation = "VI";
	    			break;
	    		case "Virginia":
	    			stateAbbreviation = "VA";
	    			break;
	    		case "Washington":
	    			stateAbbreviation = "WA";
	    			break;
	    		case "West Virginia":
	    			stateAbbreviation = "WV";
	    			break;
	    		case "Wisconsin":
	    			stateAbbreviation = "WI";
	    			break;
	    		case "Wyoming":
	    			stateAbbreviation = "WY";
	    			break;
	    		case "Armed Forces Americas":
	    			stateAbbreviation = "AA";
	    			break;
	    		case "Armed Forces Africa":
	    			stateAbbreviation = "AE";
	                break;
	    		case "Armed Forces Canada":
	    			stateAbbreviation = "AE";
	    			break;
	    		case "Armed Forces Europe":
	    			stateAbbreviation = "AE";
	    			break;
	    		case "Armed Forces Middle East":
	    			stateAbbreviation = "AE";
	    			break;
	    		case "Armed Forces Pacific":
	    			stateAbbreviation = "AP";
	    			break;
	    		default:
	    			stateAbbreviation = "Select";
	    			break;
	    	}
	    	
			return stateAbbreviation;
	    	
	    }
}


