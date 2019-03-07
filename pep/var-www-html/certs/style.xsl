<xsl:stylesheet version = "1.0" xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">
	<xsl:template match="/">
	<xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
	<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
		<html>
			<xsl:for-each select="/saml2:Assertion">
				<head>
					<title>Certificate</title>
				</head>
				<body>
					<style>
						.label {
								font-weight: bold;
								vertical-align: text-top;
								text-align: right;
								padding-left:2%;
								padding-right:2%;
								color:white;
								width: 36%;
						}
						table{
								table-layout:fixed;
								width:100%;
								border-collapse: collapse;
						}
						td{
								color:white;
								word-wrap:break-word;
						}
						body{
								font-family:Arial;
								font-size:12pt;
						}
						.cert{
								background-color:#589ebd;;
								border-style:solid;
								border-width:3px;
								clear:both;
								border: 5px solid #dadada;
								padding:50px;
								width:67%;
								margin:4% auto;
						}
						h1{
								text-align:center;
						}
						tr.spaceUnder>td{
							padding-bottom: 1em;
						}
					</style>
					<div class="cert">
						<xsl:choose>
                                                        <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']">
								<h1>Certificate <xsl:value-of select="./@ID" /></h1>
                                                        </xsl:when>
                                                        <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']">
								<h1>Certificate <xsl:value-of select="./@ID" /> (Proof)</h1>
                                                        </xsl:when>
                                                        <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']">
								<h1>Certificate <xsl:value-of select="./@ID" /> (Insurance)</h1>
                                                        </xsl:when>
                                                        <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']">
								<h1>Certificate <xsl:value-of select="./@ID" /> (Pentest)</h1>
                                                        </xsl:when>
                                                </xsl:choose>






						<table class="topTable">
							<tbody>
								<tr>
									<td>
										<table>
											<tr>
												<td class="label">Sent:</td>
												<td>
													<xsl:value-of select="./@IssueInstant" />
												</td>
											</tr>
											<tr>
												<td class="label">Sender:</td>
												<td>
													<xsl:value-of select="saml2:Issuer" />
												</td>
											</tr>
											<tr>
												<td class="label">Effective:</td>
												<td>
													<xsl:value-of select="saml2:Conditions/@NotBefore" />
												</td>
											</tr>
											<tr>
												<td class="label">Expires:</td>
												<td>
													<xsl:value-of select="saml2:Conditions/@NotOnOrAfter" />
												</td>
											</tr>
											<tr>
												<td class="label"><xsl:value-of select="translate(saml2:Subject/saml2:NameID/@NameQualifier, $lowercase, $uppercase)" />:</td>
												<td>
													<xsl:value-of select="saml2:Subject/saml2:NameID" />
												</td>
											</tr>
										</table>
									</td>
									<td>
										<table>
											<tr>
												<td class="label">Audience:</td>
												<td>
													<xsl:value-of select="saml2:Conditions/saml2:AudienceRestriction/saml2:Audience" />
												</td>
											</tr>
											<tr>
												<td class="label">Name:</td>
												<td>
													<xsl:choose>
														<xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']">
															<xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']/saml2:AttributeValue/name" />
														</xsl:when>
														<xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']/saml2:AttributeValue/name" />
                                                                                                                </xsl:when>
														<xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']/saml2:AttributeValue/name" />
                                                                                                                </xsl:when>
														<xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']/saml2:AttributeValue/name" />
                                                                                                                </xsl:when>
													</xsl:choose>
												</td>
											</tr>
											<tr>
												<td class="label">Length:</td>
												<td>
													<xsl:choose>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']/saml2:AttributeValue/length" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']/saml2:AttributeValue/length" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']/saml2:AttributeValue/length" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']/saml2:AttributeValue/length" />
                                                                                                                </xsl:when>
                                                                                                        </xsl:choose>
												</td>
											</tr>
											<tr>
												<td class="label">Version:</td>
												<td>
													<xsl:choose>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']/saml2:AttributeValue/version" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']/saml2:AttributeValue/version" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']/saml2:AttributeValue/version" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']/saml2:AttributeValue/version" />
                                                                                                                </xsl:when>
                                                                                                        </xsl:choose>
												</td>
											</tr>
											<tr>
												<td class="label">Description:</td>
												<td>
													<xsl:choose>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']/saml2:AttributeValue/description" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']/saml2:AttributeValue/description" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']/saml2:AttributeValue/description" />
                                                                                                                </xsl:when>
                                                                                                                <xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']">
                                                                                                                        <xsl:value-of select="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']/saml2:AttributeValue/description" />
                                                                                                                </xsl:when>
                                                                                                        </xsl:choose>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</tbody>
						</table>
						<table>
							<tbody>
								<hr/>
								<xsl:choose>
									<xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']">
										<xsl:apply-templates select="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']/saml2:AttributeValue/property_info"/>
									</xsl:when>
									<xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']">
										<xsl:apply-templates select="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']/saml2:AttributeValue"/>
									</xsl:when>
									<xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']">
										<xsl:apply-templates select="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']/saml2:AttributeValue"/>
									</xsl:when>
									<xsl:when test="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']">
                                                                                <xsl:apply-templates select="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']/saml2:AttributeValue"/>
                                                                        </xsl:when>
								</xsl:choose>
							</tbody>
						</table>
					</div>
				</body>
			</xsl:for-each>
		</html>
	</xsl:template>

	<xsl:template match="saml2:AttributeStatement/saml2:Attribute[@Name='insurance-info']/saml2:AttributeValue">
                <tr>
                        <td>
                                <table>
                                        <xsl:for-each select="policy/properties/property">
                                                <tr>
                                                        <td class="label" style="width:30%;">Property:</td>
                                                        <td><xsl:value-of select="name" /></td>
                                                </tr>
                                                <tr class="spaceUnder">
                                                        <td/>
                                                        <td><xsl:value-of select="description" /></td>
                                                </tr>
                                        </xsl:for-each>
                                </table>
                        </td>
			<td>
				<table>
					<tr>
                                                <td class="label">Company Name:</td>
                                                <td><xsl:value-of select="provider/company_name" /></td>
	                                </tr>
					<tr>
                                                <td class="label">Agent Name:</td>
                                                <td><xsl:value-of select="provider/agent_name" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Address:</td>
                                                <td><xsl:value-of select="provider/address" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Phone:</td>
                                                <td><xsl:value-of select="provider/phone" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Email:</td>
                                                <td><xsl:value-of select="provider/email" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Policy Conditions:</td>
                                                <td>
                                                        <table>
                                                                <xsl:for-each select="policy/conditions/condition">
                                                                        <tr><td><xsl:value-of select="." /></td></tr>
                                                                </xsl:for-each>
                                                        </table>
                                                </td>
                                        </tr>
					<tr>
                                                <td class="label">Amount Insured:</td>
                                                <td><xsl:value-of select="policy/amount_insured" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Full Policy:</td>
                                                <td><a href="{policy/full_policy}"><xsl:value-of select="policy/full_policy" /></a></td>
                                        </tr>
				</table>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="saml2:AttributeStatement/saml2:Attribute[@Name='pentest-info']/saml2:AttributeValue">
                <tr>
                        <td>
                                <table>
                                        <xsl:for-each select="properties/property">
                                                <tr>
                                                        <td class="label" style="width:30%;">Property:</td>
                                                        <td><xsl:value-of select="name" /></td>
                                                </tr>
                                                <tr class="spaceUnder">
                                                        <td class="label">Description:</td>
                                                        <td><xsl:value-of select="description" /></td>
                                                </tr>
                                        </xsl:for-each>
                                </table>
                        </td>
                        <td>
                                <table>
                                        <tr>
                                                <td class="label">Tester Name:</td>
						<td><xsl:value-of select="pentester/name" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Organization:</td>
                                                <td><xsl:value-of select="pentester/organization" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Email:</td>
                                                <td><xsl:value-of select="pentester/email" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Website:</td>
                                                <td><a href="{pentester/website}"><xsl:value-of select="pentester/website" /></a></td>
                                        </tr>
				</table>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="saml2:AttributeStatement/saml2:Attribute[@Name='proof-info']/saml2:AttributeValue">
		<tr>
			<td>
				<table>
					<xsl:for-each select="properties/property">
						<tr>
			                                <td class="label" style="width:30%;">Property:</td>
			                                <td><xsl:value-of select="name" /></td>
			                        </tr>
			                        <tr class="spaceUnder">
			                                <td/>
			                                <td><xsl:value-of select="description" /></td>
			                        </tr>
					</xsl:for-each>
				</table>
			</td>
			<td>
				<table>
					<tr>
						<td class="label">File:</td>
						<td><xsl:value-of select="file" /></td>
					</tr>
					<tr>
                                                <td class="label">Dependencies:</td>
                                                <td>
							<table>
								<xsl:for-each select="dependencies/dependency">
									<tr><td><xsl:value-of select="." /></td></tr>
								</xsl:for-each>
							</table>
						</td>
                                        </tr>
					<tr>
						<td class="label">Checker Name:</td>
						<td><xsl:value-of select="checker/name" /></td>
					</tr>
					<tr>
                                                <td class="label">Checker Version:</td>
                                                <td><xsl:value-of select="checker/version" /></td>
                                        </tr>
					<tr>
                                                <td class="label">Checker Args:</td>
                                                <td>
                                                        <table>
                                                                <xsl:for-each select="checker/args/arg">
                                                                        <tr><td><xsl:value-of select="." /></td></tr>
                                                                </xsl:for-each>
                                                        </table>
                                                </td>
                                        </tr> 
				</table>
			</td> 
		</tr>
	</xsl:template>

	<xsl:template match="saml2:AttributeStatement/saml2:Attribute[@Name='cert-info']/saml2:AttributeValue/property_info">
		<xsl:for-each select="entry">
			<tr>
				<td class="label" style="width:30%;">Property:</td>
				<td><xsl:value-of select="property" /></td>
			</tr>
			<xsl:if test="proof">
				<tr class="spaceUnder">
					<td/>
					<td>Proof: <a href="{proof}"><xsl:value-of select="proof" /></a></td>
				</tr>
			</xsl:if>
			<xsl:if test="insured">
				<tr class="spaceUnder">
					<td/>
					<td>Insurance: <a href="{insured}"><xsl:value-of select="insured" /></a></td>
				</tr>
			</xsl:if>
			<xsl:if test="reference">
                                <tr class="spaceUnder">
                                        <td/>
                                        <td>Reference: <a href="{reference}"><xsl:value-of select="reference" /></a></td>
                                </tr>
                        </xsl:if>
			<xsl:if test="pentested">
                                <tr class="spaceUnder">
                                        <td/>
                                        <td>Pentested: <a href="{pentested}"><xsl:value-of select="pentested" /></a></td>
                                </tr>
                        </xsl:if>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>
