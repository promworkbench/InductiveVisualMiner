varying vec2 texture_coordinate;

in vec4 fColor;

vec4 getCircleColor(vec2 texCoord)
{
	float cutOff = 0.49;
	float borderCutOff = 0.45;
	float fillGradientCutOff = 0.2;
	
	vec4 fillColourOuter = vec4(fColor.rgb, 1);
	vec4 fillColourInner = vec4(1, 1, 1, 1);
	vec4 strokeColour = vec4(0, 0, 0, 1);
	float opacity = fColor.a;
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texCoord)*1.5;
	
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
	float cutOff = 0.49;
	float fillGradientCutOff = 0.49;
	
	vec4 fillColourOuter = vec4(0, 0, 0, 1);
	vec4 fillColourInner = vec4(0, 0, 0, 1);
	float opacity = fColor.a;
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texCoord)*1.5;
	
	float fillGradient = smoothstep(0.0, fillGradientCutOff, dist);
	
	//perform a kind-of anti-aliasing
	float smoothing = 0.7 * length(vec2(dFdx(dist), dFdy(dist)));
	float val = smoothstep(cutOff - smoothing, cutOff + smoothing, dist);
	
	//determine whether we are on the border
	float isFill = 1 - val; 
	
	float R = mix(mix(fillColourInner.r, fillColourOuter.r, fillGradient), 1, 1-isFill);
	float G = mix(mix(fillColourInner.g, fillColourOuter.g, fillGradient), 1, 1-isFill);
	float B = mix(mix(fillColourInner.b, fillColourOuter.b, fillGradient), 1, 1-isFill);
	float A = opacity * (1-smoothstep(cutOff-0.2, cutOff, dist));
	
	//return smoothstep(smoothstep(vec4(0,0,0,1), vec4(0,0,0,0.2), vec4(fillGradient,fillGradient,fillGradient,fillGradient)), vec4(0,0,0,0), vec4(1-isFill,1-isFill,1-isFill,1-isFill));
	
	return vec4(0, 0, 0, A);
}

void main()
{
	//settings
	vec4 circleColor = getCircleColor(texture_coordinate);
	vec4 shadowColor = getShadowColor(texture_coordinate + vec2(-0.06,-0.11));
	//vec4 shadowColor = getShadowColor(texture_coordinate + vec2(-0.1,-0.15));
	//vec4 shadowColor = getShadowColor(texture_coordinate + vec2(-0.2,-0.3));
	
	gl_FragColor = mix(circleColor, shadowColor, 1-circleColor.w);
	//gl_FragDepth = 1+(1-circleColor.w);

}