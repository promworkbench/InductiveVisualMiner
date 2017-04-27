varying vec2 texture_coordinate;

in vec4 fColor;

void main()
{
	//settings
	float cutOff = 0.49;
	float borderCutOff = 0.38;
	float fillGradientCutOff = 0.2;
	
	vec4 fillColourOuter = vec4(fColor.rgb, 1);
	vec4 fillColourInner = vec4(1, 1, 1, 1);
	vec4 strokeColour = vec4(0, 0, 0, 1);
	float opacity = fColor.a;
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texture_coordinate);
	
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
	gl_FragColor = vec4(R, G, B, A);

}