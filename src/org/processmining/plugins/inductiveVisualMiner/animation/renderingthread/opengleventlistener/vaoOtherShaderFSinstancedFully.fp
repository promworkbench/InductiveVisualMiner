varying vec2 texture_coordinate;
flat in int instanceID;

in vec4 fColor;

vec4 getCircleColor2(vec2 texCoord)
{
	float cutOff = 0.9;
	
	vec4 fillColourIn = vec4(fColor.rgb, 1);
	vec4 backgroundColour = vec4(1, 1, 1, 0);
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texCoord);
	
	//perform a kind-of anti-aliasing
	float smoothing = 0.01;
	float inOut = smoothstep(cutOff - smoothing, cutOff + smoothing, dist);
	
	return mix(fillColourIn, backgroundColour, inOut);
}

vec4 getHighlightColour(vec2 texCoord) {
	float cutOff = 0.3;
	
	vec4 fillColourIn = vec4(1, 1, 1, 0.9);
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texCoord);
	
	//perform a kind-of anti-aliasing
	float smoothing = 0.01;
	float inOut = 1-smoothstep(0.01, cutOff, dist);
	
	return vec4(fillColourIn.rgb, inOut);
}

vec4 getShadowColour(vec2 texCoord) {
	float cutOff = 1.5;
	
	vec4 fillColourIn = vec4(0, 0, 0, 0.2);
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texCoord);
	
	//perform a kind-of anti-aliasing
	float smoothing = 0.01;
	float inOut = smoothstep(0.2, cutOff, dist) * (1-step(cutOff, dist));
	
	return vec4(fillColourIn.rgb, inOut);
}


void main()
{
	vec4 highlightColour = getHighlightColour(texture_coordinate + vec2(0.2, 0.3));
	
	vec4 shadowColour = getShadowColour(texture_coordinate + vec2(0.15, 0.1));
	
	vec4 circleColour = getCircleColor2(texture_coordinate);
	
	vec4 shadowHighlightColour = mix(highlightColour, shadowColour, shadowColour.a);
	//vec4 shadowHighlightColour = shadowColour;
	
	gl_FragColor = mix(shadowHighlightColour, circleColour, 1-shadowHighlightColour.a);
	//gl_FragColor = shadowHighlightColour;
	float opacity = fColor.a;
	gl_FragColor.a = opacity * circleColour.a;
	
	return;
	
	vec4 shadowColor = getShadowColor(texture_coordinate + vec2(-0.1,-0.15));
	circleColour = getCircleColor(texture_coordinate, shadowColor);
	
	gl_FragColor = mix(circleColour, shadowColor, 1-circleColour.w);
	
	float depthOfId = 1/instanceID * 0.5;
	
	float inCircle = step(0.99, circleColour.w);
	float inShadow = step(0.01, shadowColor.w);
	float inShadowOnly = (1-inCircle) * inShadow;
	float notInCircle = 1-inCircle;
	float outBoth = (1-inCircle) * (1-inShadow);
	
	if (outBoth > 0.99) {
		discard;
	}
	
	//gl_FragDepth = ((1-inShadowOnly) * depthOfId) + ((inShadowOnly) * (depthOfId + 0.5));
	//gl_FragDepth = ((1-notInCircle) * depthOfId) + ((notInCircle) * (depthOfId + 0.5));
	//gl_FragDepth = ((1-notInCircle) * depthOfId) + ((notInCircle) * (depthOfId + 0.5)) + ((outBoth) * 1);
	//gl_FragColor = vec4(0, 0, outBoth, 1);
}