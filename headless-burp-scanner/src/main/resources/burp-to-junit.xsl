<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:base64="eu.nets.burp.JUnitXmlGenerator">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
    <testsuites>
      <xsl:variable name="numberOfTests" select="count(//issues/issue)"/>
      <xsl:variable name="numberOfFailures" select="count(//issues/issue)"/>
      <testsuite name="BurpSuite"
                 tests="{$numberOfTests}"
                 failures="{$numberOfFailures}" errors="0"
                 skipped="0"
                 time="0">
        <xsl:for-each select="//issues/issue">
          <xsl:variable name="newline">
            <xsl:text>&#10;</xsl:text>
          </xsl:variable>
          <xsl:variable name="testName" select="translate(name, '-', '_')"/>
          <xsl:variable name="type" select="type"/>
          <xsl:variable name="message"
                        select="concat('Type: ', $type, $newline, 'Host: ', host, $newline, 'Location: ', location, $newline, $newline, 'Background: ', issueBackground, $newline, $newline, 'Remediation: ', remediationBackground)"/>
          <xsl:variable name="location" select="concat(host, location)"/>
          <testcase classname="{$testName}" name="{$location}">
            <failure type="{$type}" message="{$message}">
            </failure>
            <system-out>
              <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
              <xsl:text>Request:</xsl:text>
              <xsl:value-of select="$newline" disable-output-escaping="no"/>
              <xsl:value-of disable-output-escaping="yes" select="base64:decodeBase64(requestresponse/request)"/>
<!--              <xsl:text>Response:</xsl:text>
              <xsl:value-of select="$newline" disable-output-escaping="no"/>
              <xsl:choose>
                <xsl:when test="string-length(base64:decodeBase64(requestresponse/response)) &gt; 1000">
                  <xsl:value-of disable-output-escaping="no"
                                select="substring(base64:decodeBase64(requestresponse/response), 0, 1000)"/>...
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of disable-output-escaping="no" select="base64:decodeBase64(requestresponse/response)"/>
                </xsl:otherwise>
              </xsl:choose>-->
              <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
            </system-out>
          </testcase>
        </xsl:for-each>
      </testsuite>
    </testsuites>
  </xsl:template>
</xsl:stylesheet>
