varying vec2 texture_coordinate;

in vec4 fColor;

float sdSphere( vec2 p, float size, float smoothing ) {
	return smoothstep(length(p) - smoothing, length(p) + smoothing, size);
}

float sdSquare( vec2 p, float s) {
	return step(max(abs(p.x), abs(p.y)), s);
}

float sdRectangle( vec2 p, vec2 topRight, vec2 bottomLeft ) {
	return min(min(step(topRight.y, p.y), step(p.y, bottomLeft.y) ) , min(step(topRight.x, p.x), step(p.x, bottomLeft.x) ) );  
}

vec4 max4(vec4 a, vec4 b) {
	return vec4(max(a.x, b.x), max(a.y, b.y), max(a.z, b.z), max(a.w, b.w));
}

void main()
{
	
	//settings
	float cutOff = 0.49;
	float borderCutOff = 0.38;
	float fillGradientCutOff = 0.2;
	
	vec4 fillColourOuter = fColor;
	vec4 fillColourInner = vec4(1, 1, 1, 1);
	vec4 strokeColour = vec4(0, 0, 0, 1);
	vec4 goldColour = vec4(1, .843, 0, 1);
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
	//gl_FragColor = vec4(R, G, B, A);

	//float outerSquare = 1-sdSquare(texture_coordinate, 0.9);
	float sphere = sdSphere(texture_coordinate, 0.8, 0.01);
	float rectangle = sdRectangle(texture_coordinate, vec2(-0.15, -0.9), vec2(0.15, 0));
	float goldPiece = sdRectangle(texture_coordinate, vec2(-0.15, -1), vec2(0.15, -0.9));
	vec4 ball = fillColourOuter * vec4(1, 1, 1, max(sphere, rectangle)) + goldPiece * goldColour;
	
	//highlight
	float h = sdSphere(texture_coordinate + vec2(-0.2,0.2), 0.15, 0.13);
	vec4 highlight = vec4(h, h, h, h);
	
	gl_FragColor = max4(ball, highlight);
}