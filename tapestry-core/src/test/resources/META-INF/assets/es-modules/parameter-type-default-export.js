export default function(nullValue, trueValue, falseValue, piTimesE, stringValue, jsonLiteralValue,
	objectValue, arrayValue) {
		
	if (nullValue === null && (typeof trueValue === "boolean") && trueValue === true &&
		(typeof falseValue === "boolean") && falseValue === false &&
		(typeof piTimesE === "number") && piTimesE === Math.PI * Math.E &&
		(typeof jsonLiteralValue === "string") && jsonLiteralValue === "jsonLiteral" &&
		(typeof objectValue === "object") && objectValue.key === "value" &&
		arrayValue.constructor === Array && arrayValue[0] === 1 && arrayValue[1] === "2") {
			
		document.getElementById("parameter-type-default-export-message").innerHTML = "Parameter types passed correctly!";
		
	}
}