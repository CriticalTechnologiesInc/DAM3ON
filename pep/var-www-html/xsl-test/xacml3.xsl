<?xml version="1.0" encoding="UTF-8"?>
<html xsl:version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <body style="font-family:Arial;font-size:12pt;background-color:555555;">

        <xsl:for-each select="PolicySet">
            <div style="border: 5px solid #CCCDBB;padding:5px; ">
				<div style="font-size:16pt;color:white;">BEGIN POLICY SET</div>
                <div style="font-size:16pt;color:white;">Showing contents of: <span style="color:powderblue;"><xsl:value-of select="@PolicySetId"/></span> policy set.</div>
                <xsl:if test="Description"><div style="color:white;padding:4px;font-weight:bold;"> Description: <xsl:value-of select="Description"/></div></xsl:if>

                <br/>

                <xsl:for-each select="Target">
                    <div style="margin-left:20px;margin-right:20px;border: 2px solid #dd7788;">
                        <p style="color:white;padding:4px;font-weight:bold;"> BEGIN TARGET </p>
                        <xsl:for-each select="AnyOf/AllOf/Match">
                            <div style="color:white;padding:4px">
                                <span style="font-weight:bold; margin-left:20px;"><xsl:value-of select="AttributeDesignator/@AttributeId"/> <span style="color:#7edede;padding-left:5px;padding-right:5px;">&#8594;</span> <xsl:value-of select="AttributeValue"/></span>
                            </div>
                        </xsl:for-each>
                        <p style="color:white;padding:4px;font-weight:bold;"> END TARGET </p>
                    </div><br/>
                </xsl:for-each>

                <xsl:for-each select="Policy">
                    <div style="margin-left:20px;margin-right:20px;border: 2px solid #f2e090;">
                        <p style="color:white;padding:4px;font-weight:bold;"> BEGIN POLICY </p>

                        <xsl:for-each select="Target">
                            <div style="margin-left:20px;margin-right:20px;border: 2px solid #dd7788;">
                                <p style="color:white;padding:4px;font-weight:bold;"> BEGIN TARGET </p>

                                <xsl:for-each select="AnyOf/AllOf/Match">
                                    <div style="color:white;padding:4px">
                                        <span style="font-weight:bold; margin-left:20px;"><xsl:value-of select="AttributeDesignator/@AttributeId"/> <span style="color:#7edede;padding-left:5px;padding-right:5px;">&#8594;</span> <xsl:value-of select="AttributeValue"/></span>
                                    </div>
                                </xsl:for-each>

                                <p style="color:white;padding:4px;font-weight:bold;"> END TARGET </p>
                            </div><br/>
                        </xsl:for-each>

                        <xsl:for-each select="Rule">
                            <div style="margin-left:20px;margin-right:20px;border: 2px solid #667799;">
                                <p style="color:white;padding:4px;font-weight:bold;"> BEGIN RULE WITH EFFECT: <xsl:value-of select="@Effect"/></p>
                                <xsl:if test="@RuleId"><div style="color:white;padding:4px;font-weight:bold; margin-left:20px;"> RuleId: <xsl:value-of select="@RuleId"/></div></xsl:if>
                                <xsl:if test="Description"><div style="color:white;padding:4px;font-weight:bold; margin-left:20px;"> Description: <xsl:value-of select="Description"/></div></xsl:if>
                                <xsl:for-each select="Target">
                                    <div style="margin-left:20px;margin-right:20px;border: 2px solid #7a9460;">
                                        <p style="color:white;padding:4px;font-weight:bold;"> BEGIN TARGET </p>
                                            <xsl:for-each select="AnyOf/AllOf/Match">
                                                <div style="color:white;padding:4px">
                                                    <span style="font-weight:bold; margin-left:20px;"><xsl:value-of select="AttributeDesignator/@AttributeId"/> <span style="color:#7edede;padding-left:5px;padding-right:5px;">&#8594;</span> <xsl:value-of select="AttributeValue"/></span>
                                                </div>
                                            </xsl:for-each>
                                        <p style="color:white;padding:4px;font-weight:bold;"> END TARGET </p>
                                    </div>
                                </xsl:for-each>

                                <br/>

                                <xsl:for-each select="Condition/Apply">
                                    <div style="margin-left:20px;margin-right:20px;border: 2px solid #dd9977;">
                                        <p style="color:white;padding:4px;font-weight:bold;"> BEGIN CONDITIONS </p>

                                        <div style="color:white;font-weight:bold; margin-left:16px; margin-bottom:15px; margin-top:15px; padding:4px; border: 2px solid #f2e090;">Function: <xsl:value-of select="@FunctionId"/>
                                        <xsl:for-each select="AttributeValue">
                                            <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeValue: <xsl:value-of select="."/></div>
                                        </xsl:for-each>
                                        <xsl:for-each select="AttributeDesignator/@AttributeId">
                                            <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeDesignator: <xsl:value-of select="."/></div>
                                        </xsl:for-each>

                                        <xsl:for-each select="Apply">
											<div style="color:white;font-weight:bold; margin-left:16px; margin-bottom:15px; margin-top:15px; padding:4px; border: 2px solid #83adb5;">
                                            <xsl:if test="@FunctionId">Function: <xsl:value-of select="@FunctionId"/></xsl:if>
                                            <xsl:for-each select="AttributeValue">
                                                <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeValue: <xsl:value-of select="."/></div>
                                            </xsl:for-each>
                                            <xsl:for-each select="AttributeDesignator/@AttributeId">
                                                <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeDesignator: <xsl:value-of select="."/></div>
                                            </xsl:for-each>

                                            <xsl:for-each select="Apply">
											<div style="color:white;font-weight:bold; margin-left:16px; margin-bottom:15px; margin-top:15px; padding:4px; border: 2px solid #c7bbc9;">
                                                <xsl:if test="@FunctionId">Function: <xsl:value-of select="@FunctionId"/></xsl:if>
                                                  <xsl:for-each select="AttributeValue">
                                                    <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeValue: <xsl:value-of select="."/></div>
                                                </xsl:for-each>
                                                <xsl:for-each select="AttributeDesignator/@AttributeId">
                                                    <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeDesignator: <xsl:value-of select="."/></div>
                                                </xsl:for-each>
                                                </div>
                                            </xsl:for-each>
                                                </div>
                                        </xsl:for-each>
                                            </div>

                                        <p style="color:white;padding:4px;font-weight:bold;"> END CONDITIONS </p>
                                    </div>
                                </xsl:for-each>

                                <p style="color:white;padding:4px;font-weight:bold;"> END RULE</p>
                                </div><br/>

                        </xsl:for-each>

						<xsl:for-each select="AdviceExpressions">
							<div style="margin-left:20px;margin-right:20px;border: 2px solid #7a9460;">
							<p style="color:white;padding-left:4px;font-weight:bold;"> BEGIN ADVICE </p>
							<div style="color:white;padding:4px;font-weight:bold; margin-left:20px;">
							
							<xsl:for-each select="AdviceExpression">
							<div style="border: 2px solid #c7bbc9; margin-bottom:15px;">
								<p style="color:white;padding:4px;font-weight:bold;"> Advice is for: <xsl:value-of select="@AppliesTo"/> </p>
								<xsl:for-each select="AttributeAssignmentExpression/AttributeValue">
									<div style="color:white;font-weight:bold; margin-left:16px; padding:4px;"><xsl:value-of select="@AttributeId"/> <span style="color:#7edede;padding-left:5px;padding-right:5px;">&#8594;</span> <xsl:value-of select="."/></div>
								</xsl:for-each>
								</div>
							</xsl:for-each>
							</div>
							<p style="color:white;padding:4px;font-weight:bold;"> END ADVICE </p>
							</div>
						</xsl:for-each>
						
                        <p style="color:white;padding:4px;font-weight:bold;"> END POLICY </p>
                    </div><br/>
                </xsl:for-each>
			<div style="font-size:16pt;color:white;">END POLICY SET</div>
            </div>
        </xsl:for-each>

		<!-- below is code for XACML that only contains a "Policy" at the top level. Should be a duplicate of the XSL for Policy witihin PolicySet -->
		 <xsl:for-each select="Policy">
                    <div style="margin-left:20px;margin-right:20px;border: 2px solid #f2e090;">
                        <p style="color:white;padding:4px;font-weight:bold;"> BEGIN POLICY </p>

                        <xsl:for-each select="Target">
                            <div style="margin-left:20px;margin-right:20px;border: 2px solid #dd7788;">
                                <p style="color:white;padding:4px;font-weight:bold;"> BEGIN TARGET </p>

                                <xsl:for-each select="AnyOf/AllOf/Match">
                                    <div style="color:white;padding:4px">
                                        <span style="font-weight:bold; margin-left:20px;"><xsl:value-of select="AttributeDesignator/@AttributeId"/> <span style="color:#7edede;padding-left:5px;padding-right:5px;">&#8594;</span> <xsl:value-of select="AttributeValue"/></span>
                                    </div>
                                </xsl:for-each>

                                <p style="color:white;padding:4px;font-weight:bold;"> END TARGET </p>
                            </div><br/>
                        </xsl:for-each>

                        <xsl:for-each select="Rule">
                            <div style="margin-left:20px;margin-right:20px;border: 2px solid #667799;">
                                <p style="color:white;padding:4px;font-weight:bold;"> BEGIN RULE WITH EFFECT: <xsl:value-of select="@Effect"/></p>
                                <xsl:if test="@RuleId"><div style="color:white;padding:4px;font-weight:bold; margin-left:20px;"> RuleId: <xsl:value-of select="@RuleId"/></div></xsl:if>
                                <xsl:if test="Description"><div style="color:white;padding:4px;font-weight:bold; margin-left:20px;"> Description: <xsl:value-of select="Description"/></div></xsl:if>
                                <xsl:for-each select="Target">
                                    <div style="margin-left:20px;margin-right:20px;border: 2px solid #7a9460;">
                                        <p style="color:white;padding:4px;font-weight:bold;"> BEGIN TARGET </p>
                                            <xsl:for-each select="AnyOf/AllOf/Match">
                                                <div style="color:white;padding:4px">
                                                    <span style="font-weight:bold; margin-left:20px;"><xsl:value-of select="AttributeDesignator/@AttributeId"/> <span style="color:#7edede;padding-left:5px;padding-right:5px;">&#8594;</span> <xsl:value-of select="AttributeValue"/></span>
                                                </div>
                                            </xsl:for-each>
                                        <p style="color:white;padding:4px;font-weight:bold;"> END TARGET </p>
                                    </div>
                                </xsl:for-each>

                                <br/>

                                <xsl:for-each select="Condition/Apply">
                                    <div style="margin-left:20px;margin-right:20px;border: 2px solid #dd9977;">
                                        <p style="color:white;padding:4px;font-weight:bold;"> BEGIN CONDITIONS </p>

                                        <div style="color:white;font-weight:bold; margin-left:16px; margin-bottom:15px; margin-top:15px; padding:4px; border: 2px solid #f2e090;">Function: <xsl:value-of select="@FunctionId"/>
                                        <xsl:for-each select="AttributeValue">
                                            <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeValue: <xsl:value-of select="."/></div>
                                        </xsl:for-each>
                                        <xsl:for-each select="AttributeDesignator/@AttributeId">
                                            <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeDesignator: <xsl:value-of select="."/></div>
                                        </xsl:for-each>

                                        <xsl:for-each select="Apply">
											<div style="color:white;font-weight:bold; margin-left:16px; margin-bottom:15px; margin-top:15px; padding:4px; border: 2px solid #83adb5;">
                                            <xsl:if test="@FunctionId">Function: <xsl:value-of select="@FunctionId"/></xsl:if>
                                            <xsl:for-each select="AttributeValue">
                                                <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeValue: <xsl:value-of select="."/></div>
                                            </xsl:for-each>
                                            <xsl:for-each select="AttributeDesignator/@AttributeId">
                                                <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeDesignator: <xsl:value-of select="."/></div>
                                            </xsl:for-each>

                                            <xsl:for-each select="Apply">
											<div style="color:white;font-weight:bold; margin-left:16px; margin-bottom:15px; margin-top:15px; padding:4px; border: 2px solid #c7bbc9;">
                                                <xsl:if test="@FunctionId">Function: <xsl:value-of select="@FunctionId"/></xsl:if>
                                                  <xsl:for-each select="AttributeValue">
                                                    <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeValue: <xsl:value-of select="."/></div>
                                                </xsl:for-each>
                                                <xsl:for-each select="AttributeDesignator/@AttributeId">
                                                    <div style="color:white;font-weight:bold; margin-left:16px; padding:4px;">AttributeDesignator: <xsl:value-of select="."/></div>
                                                </xsl:for-each>
                                                </div>
                                            </xsl:for-each>
                                                </div>
                                        </xsl:for-each>
                                            </div>

                                        <p style="color:white;padding:4px;font-weight:bold;"> END CONDITIONS </p>
                                    </div>
                                </xsl:for-each>

                                <p style="color:white;padding:4px;font-weight:bold;"> END RULE</p>
                                </div><br/>

                        </xsl:for-each>

						<xsl:for-each select="AdviceExpressions">
							<div style="margin-left:20px;margin-right:20px;border: 2px solid #7a9460;">
							<p style="color:white;padding-left:4px;font-weight:bold;"> BEGIN ADVICE </p>
							<div style="color:white;padding:4px;font-weight:bold; margin-left:20px;">
							
							<xsl:for-each select="AdviceExpression">
							<div style="border: 2px solid #c7bbc9; margin-bottom:15px;">
								<p style="color:white;padding:4px;font-weight:bold;"> Advice is for: <xsl:value-of select="@AppliesTo"/> </p>
								<xsl:for-each select="AttributeAssignmentExpression/AttributeValue">
									<div style="color:white;font-weight:bold; margin-left:16px; padding:4px;"><xsl:value-of select="@AttributeId"/> <span style="color:#7edede;padding-left:5px;padding-right:5px;">&#8594;</span> <xsl:value-of select="."/></div>
								</xsl:for-each>
								</div>
							</xsl:for-each>
							</div>
							<p style="color:white;padding:4px;font-weight:bold;"> END ADVICE </p>
							</div>
						</xsl:for-each>
						
                        <p style="color:white;padding:4px;font-weight:bold;"> END POLICY </p>
                    </div><br/>
                </xsl:for-each>
    </body>
</html>

