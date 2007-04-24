package ${packageName}.pages;

import java.util.Date;
import org.apache.tapestry.annotations.ComponentClass;

/**
 * Start page of application ${artifactId}.
 * 
 */
@ComponentClass
public class Start
{
	public Date getCurrentTime() 
	{ 
		return new Date(); 
	}
}