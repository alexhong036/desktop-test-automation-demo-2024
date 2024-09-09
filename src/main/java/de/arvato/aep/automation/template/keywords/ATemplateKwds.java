package de.arvato.aep.automation.template.keywords;

import de.arvato.aep.automation.template.impl.config.TemplateProperties;
import de.qualersoft.robotframework.library.annotation.Keyword;
import de.qualersoft.robotframework.library.annotation.KwdArg;

import javax.annotation.ManagedBean;

/**
 * This annotation is important to tell spring to respect this class in its scanning process.
 * The RobotLib (provided through AepRobotLib) than can retrieve it from spring and extract the keywords.
 */
@ManagedBean
public class ATemplateKwds {

  private final TemplateProperties config;

  public ATemplateKwds(TemplateProperties config) {
    this.config = config;
  }

  // To test the documentation, just run the 'robotLibdocHtml' task and open the 'build/robotdoc/libdoc/TemplateLib.html'
  // To test execution, just run 'allTests' task and open 'build/robot-reports/<date_time>/log.html'
  @Keyword(
      docSummary = "We can provide a summery",
      docDetails = """
          And also some more details.
          
          *Even* with _basic_ formatting."""
  )
  public void tmplSayHelloFromTheLibrary(
      @KwdArg(doc="We also can give details on the arguments.")
      String whomToGreet
  ) {
    if (null == whomToGreet || whomToGreet.isEmpty()) {
      System.out.println("I don't talk to strangers!");
    } else {
      System.out.println("""
          Hello %s,
          my magic value is %s ;)""".formatted(whomToGreet, config.getConfigValue()));
    }
  }
}
