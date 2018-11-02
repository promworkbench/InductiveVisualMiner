varying vec2 texture_coordinate;
flat in int instanceID;

in vec4 fColor;

vec4 getCircleColor(vec2 texCoord)
{
	float cutOff = 0.9;
	float borderCutOff = 0.88;
	float fillGradientCutOff = 0.2;
	
	vec4 fillColourOuter = vec4(fColor.rgb, 1);
	vec4 fillColourInner = vec4(1, 1, 1, 1);
	vec4 strokeColour = vec4(0, 0, 0, 1);
	float opacity = fColor.a;
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texCoord);
	
	float fillGradient = smoothstep(0.0, fillGradientCutOff, dist);
	
	//perform a kind-of anti-aliasing
	float smoothing = 0.7 * length(vec2(dFdx(dist), dFdy(dist)));
	float val = smoothstep(cutOff - smoothing, cutOff + smoothing, dist);
	
	//determine whether we are on the border
	float isBorder = smoothstep(borderCutOff - smoothing, borderCutOff + smoothing, dist);
	float isFill = 1 - isBorder; 
	
	float R = (isFill * (fillColourOuter.r * fillGradient) + (fillColourInner.r * (1 - fillGradient))) + (isBorder * strokeColour.r);
	float G = (isFill * (fillColourOuter.g * fillGradient) + (fillColourInner.g * (1 - fillGradient))) + (isBorder * strokeColour.g);
	float B = (isFill * (fillColourOuter.b * fillGradient) + (fillColourInner.b * (1 - fillGradient))) + (isBorder * strokeColour.b);
	float A = opacity * (1 - val) * (fillGradient + (1 - fillGradient) * fillColourInner.a);
	
	return vec4(R, G, B, A);
}

vec4 getShadowColor(vec2 texCoord)
{
	float cutOff = 0.9;
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texCoord);
	
	//opacity
	float opacity = fColor.a;
	float A = opacity * (1-smoothstep(cutOff-0.2, cutOff, dist));
	
	return vec4(0, 0, 0, A);
	
	//float fillGradientCutOff = 0.49;
	
	//vec4 fillColourOuter = vec4(0, 0, 0, 1);
	//vec4 fillColourInner = vec4(0, 0, 0, 1);
	
	//float fillGradient = smoothstep(0.0, fillGradientCutOff, dist);
	
	//perform a kind-of anti-aliasing
	//float smoothing = 0.7 * length(vec2(dFdx(dist), dFdy(dist)));
	//float val = smoothstep(cutOff - smoothing, cutOff + smoothing, dist);
	
	//determine whether we are on the border
	//float isFill = 1 - val; 
	
	//float R = mix(mix(fillColourInner.r, fillColourOuter.r, fillGradient), 1, 1-isFill);
	//float G = mix(mix(fillColourInner.g, fillColourOuter.g, fillGradient), 1, 1-isFill);
	//float B = mix(mix(fillColourInner.b, fillColourOuter.b, fillGradient), 1, 1-isFill);
	
	//return smoothstep(smoothstep(vec4(0,0,0,1), vec4(0,0,0,0.2), vec4(fillGradient,fillGradient,fillGradient,fillGradient)), vec4(0,0,0,0), vec4(1-isFill,1-isFill,1-isFill,1-isFill));
}

void main()
{
	//settings
	vec4 circleColor = getCircleColor(texture_coordinate);
	vec4 shadowColor = getShadowColor(texture_coordinate + vec2(-0.1,-0.15));
	
	gl_FragColor = mix(circleColor, shadowColor, 1-circleColor.w);
	
	float depthOfId = 1/instanceID * 0.5;
	
	float inCircle = step(0.99, circleColor.w);
	float inShadow = step(0.01, shadowColor.w);
	float inShadowOnly = (1-inCircle) * inShadow;
	float notInCircle = 1-inCircle;
	float outBoth = (1-inCircle) * (1-inShadow);
	
	gl_FragDepth = ((1-inShadowOnly) * depthOfId) + ((inShadowOnly) * (depthOfId + 0.5));
	//gl_FragDepth = ((1-notInCircle) * depthOfId) + ((notInCircle) * (depthOfId + 0.5));
	//gl_FragDepth = ((1-notInCircle) * depthOfId) + ((notInCircle) * (depthOfId + 0.5)) + ((outBoth) * 1);
	//gl_FragColor = vec4(0, 0, outBoth, 1);
}