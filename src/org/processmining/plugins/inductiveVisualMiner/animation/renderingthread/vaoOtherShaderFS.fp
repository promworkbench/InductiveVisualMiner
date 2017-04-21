varying vec2 texture_coordinate;

uniform vec3 fillColourInner;
uniform vec3 fillColourOuter;
uniform vec3 strokeColour;
uniform float opacity;

void main( )
{
	/*float dist = length(texture_coordinate - vec2(0.5,0.5));
	float smoothing = 0.7 * length(vec2(dFdx(dist), dFdy(dist)));
	float val = smoothstep(0.5 - smoothing,0.5 + smoothing, dist);
	gl_FragColor = vec4(mycolor.rgb * dist,mycolor.a * (1.0-val));*/
	
	//settings
	float cutOff = 0.49;
	float borderCutOff = 0.42;
	float fillGradientCutOff = 0.2;
	
	//compute distance from the centre (we're drawing a circle)
	float dist = length(texture_coordinate - vec2(0.5, 0.5));
	
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
	float A = opacity * (1 - val);
	gl_FragColor = vec4(R, G, B, A);
}
