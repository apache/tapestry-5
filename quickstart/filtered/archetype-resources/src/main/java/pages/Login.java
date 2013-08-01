package ${package}.pages;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class Login
{
	@Inject
	private Logger logger;

	@Inject
	private AlertManager alertManager;

	@InjectComponent
	private Form login;
	@InjectComponent
	private TextField email;
	@InjectComponent
	private PasswordField password;

	@InjectPage
	private Index index;

	@Property
	private String emailValue;
	@Property
	private String passwordValue;



	void onValidateFromLogin()
	{
		if ( !emailValue.equals("users@tapestry.apache.org"))
			login.recordError(email, "Try with user: users@tapestry.apache.org");

		if ( !passwordValue.equals("Tapestry5"))
			login.recordError(password, "Try with password: Tapestry5");
	}

	Object onSuccessFromLogin()
	{
		logger.info("Login successful!");
		alertManager.success("Welcome aboard!");

		return index;
	}

	void onFailureFromLogin()
	{
		logger.warn("Login error!");
		alertManager.error("I'm sorry but I can't log you in!");
	}

}