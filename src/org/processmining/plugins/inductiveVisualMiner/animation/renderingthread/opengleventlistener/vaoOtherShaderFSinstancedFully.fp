varying vec2 texture_coordinate;
flat in int instanceID;

in vec4 fColor;

vec4 getCircleColor2(vec2 texCoord)
{
	float cutOff = 0.9;
	
	vec4 fillColourIn = vec4(fColor.rgb, 1);
	vec4 backgroundColour = vec4(0, 0, 0, 0);
	
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
}