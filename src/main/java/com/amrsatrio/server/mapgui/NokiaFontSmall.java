package com.amrsatrio.server.mapgui;

public class NokiaFontSmall extends MapFont {
	//	private static final int[][] fontDataNokiaColor = new int[][]{
//			{0, 0b11, 0b11, 0b11, 0b01, 0b01, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // '
//			{0, 0, 0b0111110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111110, 0, 0, 0}, // 0
//			{0, 0, 0b0011000, 0b0011100, 0b0011010, 0b0011000, 0b0011000, 0b0011000, 0b0011000, 0b0011000, 0b0011000, 0b0011000, 0b1111110, 0, 0, 0}, // 1
//			{0, 0, 0b0111110, 0b1100011, 0b1100000, 0b1100000, 0b0110000, 0b0011000, 0b0001100, 0b0000110, 0b0000011, 0b0000011, 0b1111111, 0, 0, 0}, // 2
//			{0, 0, 0b0111110, 0b1100011, 0b1100000, 0b1100000, 0b1100000, 0b0111100, 0b1100000, 0b1100000, 0b1100000, 0b1100011, 0b0111110, 0, 0, 0}, // 3
//			{0, 0, 0b0110000, 0b0111000, 0b0110100, 0b0110100, 0b0110010, 0b0110010, 0b0110001, 0b1111111, 0b0110000, 0b0110000, 0b0110000, 0, 0, 0}, // 4
//			{0, 0, 0b1111111, 0b0000011, 0b0000011, 0b0000011, 0b0111111, 0b1100000, 0b1100000, 0b1100000, 0b1100000, 0b1100011, 0b0111110, 0, 0, 0}, // 5
//			{0, 0, 0b0111100, 0b0000110, 0b0000011, 0b0000011, 0b0111111, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111110, 0, 0, 0}, // 6
//			{0, 0, 0b1111111, 0b1100000, 0b0110000, 0b0110000, 0b0011000, 0b0011000, 0b0001100, 0b0001100, 0b0000110, 0b0000110, 0b0000110, 0, 0, 0}, // 7
//			{0, 0, 0b0111110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111110, 0, 0, 0}, // 8
//			{0, 0, 0b0111110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1111110, 0b1100000, 0b1100000, 0b0110000, 0b0011110, 0, 0, 0}, // 9
//			{0, 0, 0, 0, 0, 0b11, 0b11, 0, 0, 0, 0, 0b11, 0b11, 0, 0, 0}, // :
//			{0, 0, 0, 0, 0, 0b11, 0b11, 0, 0, 0, 0, 0b11, 0b11, 0b10, 0b01, 0}, // ;
//			{0, 0, 0, 0b1100000, 0b0110000, 0b0011000, 0b0001100, 0b0000110, 0b0001100, 0b0011000, 0b0110000, 0b1100000, 0, 0, 0, 0}, // <
//			{0, 0, 0, 0, 0, 0b1111110, 0b1111110, 0, 0, 0b1111110, 0b1111110, 0, 0, 0, 0, 0}, // =
//			{0, 0, 0, 0b0000110, 0b0001100, 0b0011000, 0b0110000, 0b1100000, 0b0110000, 0b0011000, 0b0001100, 0b0000110, 0, 0, 0, 0}, // >
//			{0, 0, 0b01111, 0b11000, 0b11000, 0b11000, 0b11000, 0b01100, 0b00110, 0b00110, 0, 0b00110, 0b00110, 0, 0, 0}, // ?
//			{0, 0, 0b001111100, 0b011000110, 0b110000011, 0b110110011, 0b110111011, 0b110111011, 0b110111011, 0b110111011, 0b011111011, 0b000000011, 0b011000110, 0b001111100, 0, 0}, // @
//			{0, 0, 0b0011100, 0b0110110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1111111, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0, 0, 0}, // A
//			{0, 0, 0b00111111, 0b01100011, 0b11000011, 0b11000011, 0b01100011, 0b00111111, 0b01100011, 0b11000011, 0b11000011, 0b01100011, 0b00111111, 0, 0, 0}, // B
//			{0, 0, 0b111110, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b111110, 0, 0, 0}, // C
//			{0, 0, 0b00111111, 0b01100011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b01100011, 0b00111111, 0, 0, 0}, // D
//			{0, 0, 0b111111, 0b000011, 0b000011, 0b000011, 0b000011, 0b111111, 0b000011, 0b000011, 0b000011, 0b000011, 0b111111, 0, 0, 0}, // E
//			{0, 0, 0b111111, 0b000011, 0b000011, 0b000011, 0b000011, 0b111111, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0, 0, 0}, // F
//			{0, 0, 0b1111110, 0b0000011, 0b0000011, 0b0000011, 0b0000011, 0b1110011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1011110, 0, 0, 0}, // G
//			{0, 0, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11111111, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0, 0, 0}, // H
//			{0, 0, 0b1111, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b1111, 0, 0, 0}, // I
//			{0, 0, 0b1111, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0011}, // J
//			{0, 0, 0b1100011, 0b0110011, 0b0110011, 0b0011011, 0b0011011, 0b0001111, 0b0011011, 0b0110011, 0b0110011, 0b1100011, 0b1100011, 0, 0, 0}, // K
//			{0, 0, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b000011, 0b111111, 0, 0, 0}, // L
//			{0, 0, 0b100000001, 0b110000011, 0b111000111, 0b111101111, 0b110111011, 0b110010011, 0b110000011, 0b110000011, 0b110000011, 0b110000011, 0b110000011, 0, 0, 0}, // M
//			{0, 0, 0b11000001, 0b11000011, 0b11000111, 0b11001111, 0b11001011, 0b11011011, 0b11010011, 0b11110011, 0b11100011, 0b11000011, 0b10000011, 0, 0, 0}, // N
//			{0, 0, 0b01111110, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b01111110, 0, 0, 0}, // O
//			{0, 0, 0b0011111, 0b0110011, 0b1100011, 0b1100011, 0b1100011, 0b0110011, 0b0011111, 0b0000011, 0b0000011, 0b0000011, 0b0000011, 0, 0, 0}, // P
//			{0, 0, 0b01111110, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b01111110, 0b00110000, 0b01100000, 0b11000000}, // Q
//			{0, 0, 0b00111111, 0b01100011, 0b11000011, 0b11000011, 0b01100011, 0b00111111, 0b00110011, 0b01100011, 0b01100011, 0b11000011, 0b11000011, 0, 0, 0}, // R
//			{0, 0, 0b1111110, 0b0000011, 0b0000011, 0b0000111, 0b0001110, 0b0011100, 0b0111000, 0b1110000, 0b1100000, 0b1100000, 0b0111111, 0, 0, 0}, // S
//			{0, 0, 0b11111111, 0b00011000, 0b00011000, 0b00011000, 0b00011000, 0b00011000, 0b00011000, 0b00011000, 0b00011000, 0b00011000, 0b00011000, 0, 0, 0}, // T
//			{0, 0, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b11000011, 0b01111110, 0, 0, 0}, // U
//			{0, 0, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0110110, 0b0110110, 0b0110110, 0b0011100, 0b0011100, 0, 0, 0}, // V
//			{0, 0, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b0100110010, 0b0111111110, 0b0011001100, 0b0011001100, 0, 0, 0}, // W
//			{0, 0, 0b11000011, 0b11000011, 0b01100110, 0b01100110, 0b00111100, 0b00011000, 0b00111100, 0b01100110, 0b01100110, 0b11000011, 0b11000011, 0, 0, 0}, // X
//			{0, 0, 0b11000011, 0b11000011, 0b11000011, 0b01100110, 0b01100110, 0b01100110, 0b00111100, 0b00011000, 0b00011000, 0b00011000, 0b00011000, 0, 0, 0}, // Y
//			{0, 0, 0b1111111, 0b1100000, 0b0110000, 0b0110000, 0b0011000, 0b0001100, 0b0001100, 0b0000110, 0b0000110, 0b0000011, 0b1111111, 0, 0, 0}, // Z
//			// PLS REVIEW ALL BELOW
//			{0, 0b1111, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b1111}, // [
//			{0, 0b000011, 0b000011, 0b000011, 0b000110, 0b000110, 0b000110, 0b001100, 0b001100, 0b001100, 0b011000, 0b011000, 0b011000, 0b110000, 0b110000, 0b110000}, // \
//			{0, 0b1111, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1100, 0b1111}, // ]
//			{0, 0, 0b001100, 0b001100, 0b011110, 0b010010, 0b110011, 0b110011, 0, 0, 0, 0, 0, 0, 0, 0}, // ^
//			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0b1111, 0}, // _
//			{0b0011, 0b0110, 0b1100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // `
//			{0, 0, 0, 0, 0b0111110, 0b1100000, 0b1100000, 0b1100000, 0b1111110, 0b1100011, 0b1100011, 0b1100011, 0b1011110, 0, 0, 0}, // a
//			{0, 0b0000011, 0b0000011, 0b0000011, 0b0111111, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111101, 0, 0, 0}, // b
//			{0, 0, 0, 0, 0b11110, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b11110, 0, 0, 0}, // c
//			{0, 0b1100000, 0b1100000, 0b1100000, 0b1111110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1011110, 0, 0, 0}, // d
//			{0, 0, 0, 0, 0b0111110, 0b1100011, 0b1100011, 0b1100011, 0b1111111, 0b0000011, 0b0000011, 0b0000011, 0b1111110, 0, 0, 0}, // e
//			{0, 0b11100, 0b00110, 0b00110, 0b11111, 0b00110, 0b00110, 0b00110, 0b00110, 0b00110, 0b00110, 0b00110, 0b00110, 0, 0, 0}, // f
//			{0, 0, 0, 0, 0b1011110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1111110, 0b1100000, 0b1100000, 0b0111110}, // g
//			{0, 0b0000011, 0b0000011, 0b0000011, 0b0111111, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0, 0, 0}, // h
//			{0, 0b11, 0b11, 0b00, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0, 0, 0}, // i
//			{0, 0b110, 0b110, 0b000, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b011}, // j
//			{0, 0b0000011, 0b0000011, 0b0000011, 0b1100011, 0b0110011, 0b0110011, 0b0011011, 0b0001111, 0b0011011, 0b0110011, 0b0110011, 0b1100011, 0, 0, 0}, // k
//			{0, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0, 0, 0}, // l
//			{0, 0, 0, 0, 0b0111011101, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0, 0, 0}, // m
//			{0, 0, 0, 0, 0b0111101, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0, 0, 0}, // n
//			{0, 0, 0, 0, 0b0111110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111110, 0, 0, 0}, // o
//			{0, 0, 0, 0, 0b0111101, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111111, 0b0000011, 0b0000011, 0b0000011}, // p
//			{0, 0, 0, 0, 0b1011110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1111110, 0b1100000, 0b1100000, 0b1100000}, // q
//			{0, 0, 0, 0, 0b1101, 0b1111, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0, 0, 0}, // r
//			{0, 0, 0, 0, 0b111110, 0b000011, 0b000111, 0b001110, 0b011100, 0b111000, 0b110000, 0b110000, 0b011111, 0, 0, 0}, // s
//			{0, 0, 0b00100, 0b00110, 0b11111, 0b00110, 0b00110, 0b00110, 0b00110, 0b00110, 0b00110, 0b00110, 0b11100, 0, 0, 0}, // t
//			{0, 0, 0, 0, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1011110, 0, 0, 0}, // u
//			{0, 0, 0, 0, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0110110, 0b0110110, 0b0110110, 0b0011100, 0b0011100, 0, 0, 0}, // v
//			{0, 0, 0, 0, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b0100110010, 0b0111111110, 0b0011001100, 0, 0, 0}, // w
//			{0, 0, 0, 0, 0b1100011, 0b1100011, 0b0110110, 0b0110110, 0b0011100, 0b0110110, 0b0110110, 0b1100011, 0b1100011, 0, 0, 0}, // x
//			{0, 0, 0, 0, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0110110, 0b0110110, 0b0110110, 0b0011100, 0b0001100, 0b0001100, 0b0000111, 0b0000011}, // y
//			{0, 0, 0, 0, 0b111111, 0b110000, 0b011000, 0b011000, 0b001100, 0b000110, 0b000110, 0b000011, 0b111111, 0, 0, 0}, // z
//	};
	private static final int[][] fontData = new int[][]{
			{0, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0, 0b110, 0b110, 0, 0}, // !
			{0b101, 0b101, 0b101, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // "
			{0, 0b01001000, 0b01001000, 0b11111110, 0b00100100, 0b00100100, 0b00100100, 0b01111111, 0b00010010, 0b00010010, 0, 0}, // #
			{0b00100, 0b00100, 0b11110, 0b00011, 0b00011, 0b01110, 0b11000, 0b11000, 0b01111, 0b00100, 0b00100, 0}, // $
			{0, 0b010000110, 0b001001001, 0b000101001, 0b000101001, 0b011010110, 0b100101000, 0b100101000, 0b100100100, 0b011000010, 0, 0}, // %
			{0, 0b0011110, 0b0000011, 0b0000011, 0b0110011, 0b1111110, 0b0110011, 0b0110011, 0b0110011, 0b0111110, 0, 0}, // &
			{0b1, 0b1, 0b1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // '
			{0b100, 0b110, 0b011, 0b011, 0b011, 0b011, 0b011, 0b011, 0b011, 0b011, 0b110, 0b100}, // (
			{0b001, 0b011, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b011, 0b001}, // )
			{0, 0b00100, 0b10101, 0b01110, 0b00100, 0b01110, 0b10101, 0b00100, 0, 0, 0, 0}, // *
			{0, 0, 0, 0b001000, 0b001000, 0b111110, 0b001000, 0b001000, 0, 0, 0, 0}, // +
			{0, 0, 0, 0, 0, 0, 0, 0, 0b11, 0b11, 0b10, 0b01}, // ,
			{0, 0, 0, 0, 0, 0b111, 0b111, 0, 0, 0, 0, 0}, // -
			{0, 0, 0, 0, 0, 0, 0, 0, 0b11, 0b11, 0, 0}, // .
			{0b1000, 0b1000, 0b1000, 0b0100, 0b0100, 0b0100, 0b0010, 0b0010, 0b0010, 0b0001, 0b0001, 0b0001}, // /
			{0, 0b011110, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b011110, 0, 0}, // 0
			{0, 0b001100, 0b001110, 0b001101, 0b001100, 0b001100, 0b001100, 0b001100, 0b001100, 0b111111, 0, 0}, // 1
			{0, 0b011110, 0b110011, 0b110000, 0b110000, 0b011000, 0b001100, 0b000110, 0b000011, 0b111111, 0, 0}, // 2
			{0, 0b011110, 0b110011, 0b110000, 0b110000, 0b011100, 0b110000, 0b110000, 0b110011, 0b011110, 0, 0}, // 3
			{0, 0b011000, 0b011100, 0b011010, 0b011010, 0b011001, 0b011001, 0b111111, 0b011000, 0b011000, 0, 0}, // 4
			{0, 0b111111, 0b000011, 0b000011, 0b011111, 0b110000, 0b110000, 0b110000, 0b110011, 0b011110, 0, 0}, // 5
			{0, 0b011100, 0b000110, 0b000011, 0b000011, 0b011111, 0b110011, 0b110011, 0b110011, 0b011110, 0, 0}, // 6
			{0, 0b111111, 0b110000, 0b011000, 0b011000, 0b001100, 0b001100, 0b000110, 0b000110, 0b000110, 0, 0}, // 7
			{0, 0b011110, 0b110011, 0b110011, 0b110011, 0b011110, 0b110011, 0b110011, 0b110011, 0b011110, 0, 0}, // 8
			{0, 0b011110, 0b110011, 0b110011, 0b110011, 0b111110, 0b110000, 0b110000, 0b011000, 0b001110, 0, 0}, // 9
			{0, 0, 0, 0, 0b11, 0b11, 0, 0, 0b11, 0b11, 0, 0}, // :
			{0, 0, 0, 0, 0b11, 0b11, 0, 0, 0b11, 0b11, 0b10, 0b01}, // ;
			{0, 0, 0b110000, 0b011000, 0b001100, 0b000110, 0b001100, 0b011000, 0b110000, 0, 0, 0}, // <
			{0, 0, 0, 0, 0b11111, 0, 0b11111, 0, 0, 0, 0, 0}, // =
			{0, 0, 0b000110, 0b001100, 0b011000, 0b110000, 0b011000, 0b001100, 0b000110, 0, 0, 0}, // >
			{0, 0b01111, 0b11000, 0b11000, 0b01100, 0b00110, 0b00110, 0, 0b00110, 0b00110, 0, 0}, // ?
			{0, 0b000111000, 0b001000100, 0b010000010, 0b010110010, 0b010111010, 0b010111010, 0b001111010, 0b000000010, 0b010000100, 0b001111000, 0}, // @
			{0, 0b001100, 0b011110, 0b010010, 0b110011, 0b110011, 0b110011, 0b111111, 0b110011, 0b110011, 0, 0}, // A
			{0, 0b011111, 0b110011, 0b110011, 0b110011, 0b011111, 0b110011, 0b110011, 0b110011, 0b011111, 0, 0}, // B
			{0, 0b11110, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b11110, 0, 0}, // C
			{0, 0b0011111, 0b0110011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0110011, 0b0011111, 0, 0}, // D
			{0, 0b11111, 0b00011, 0b00011, 0b00011, 0b11111, 0b00011, 0b00011, 0b00011, 0b11111, 0, 0}, // E
			{0, 0b11111, 0b00011, 0b00011, 0b00011, 0b11111, 0b00011, 0b00011, 0b00011, 0b00011, 0, 0}, // F
			{0, 0b111110, 0b000011, 0b000011, 0b000011, 0b111011, 0b110011, 0b110011, 0b110011, 0b101110, 0, 0}, // G
			{0, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1111111, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0, 0}, // H
			{0, 0b1111, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b1111, 0, 0}, // I
			{0, 0b1111, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0011}, // J
			{0, 0b110011, 0b110011, 0b011011, 0b011011, 0b001111, 0b011011, 0b011011, 0b110011, 0b110011, 0, 0}, // K
			{0, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b11111, 0, 0}, // L
			{0, 0b1000001, 0b1100011, 0b1110111, 0b1111111, 0b1101011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0, 0}, // M
			{0, 0b1100001, 0b1100011, 0b1100111, 0b1101111, 0b1101011, 0b1111011, 0b1110011, 0b1100011, 0b1000011, 0, 0}, // N
			{0, 0b0111110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111110, 0, 0}, // O
			{0, 0b011111, 0b110011, 0b110011, 0b110011, 0b110011, 0b011111, 0b000011, 0b000011, 0b000011, 0, 0}, // P
			{0, 0b0111110, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111110, 0b0011000, 0b0110000}, // Q
			{0, 0b011111, 0b110011, 0b110011, 0b110011, 0b011111, 0b110011, 0b110011, 0b110011, 0b110011, 0, 0}, // R
			{0, 0b11110, 0b00011, 0b00011, 0b00111, 0b01110, 0b11100, 0b11000, 0b11000, 0b01111, 0, 0}, // S
			{0, 0b111111, 0b001100, 0b001100, 0b001100, 0b001100, 0b001100, 0b001100, 0b001100, 0b001100, 0, 0}, // T
			{0, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b1100011, 0b0111110, 0, 0}, // U
			{0, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b010010, 0b011110, 0b001100, 0, 0}, // V
			{0, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b0100110010, 0b0111111110, 0b0011001100, 0, 0}, // W
			{0, 0b110011, 0b110011, 0b110011, 0b011110, 0b001100, 0b011110, 0b110011, 0b110011, 0b110011, 0, 0}, // X
			{0, 0b110011, 0b110011, 0b110011, 0b010010, 0b011110, 0b001100, 0b001100, 0b001100, 0b001100, 0, 0}, // Y
			{0, 0b111111, 0b110000, 0b011000, 0b011000, 0b001100, 0b000110, 0b000110, 0b000011, 0b111111, 0, 0}, // Z
			{0b111, 0b011, 0b011, 0b011, 0b011, 0b011, 0b011, 0b011, 0b011, 0b011, 0b011, 0b111}, // [
			{0b0001, 0b0001, 0b0001, 0b0010, 0b0010, 0b0010, 0b0100, 0b0100, 0b0100, 0b1000, 0b1000, 0b1000}, // \
			{0b111, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b111}, // ]
			{0, 0b001000, 0b010100, 0b010100, 0b100010, 0b100010, 0, 0, 0, 0, 0, 0}, // ^
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0b111}, // _
			{0, 0b01, 0b10, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // `
			{0, 0, 0, 0b011110, 0b110000, 0b110000, 0b111110, 0b110011, 0b110011, 0b101110, 0, 0}, // a
			{0b000011, 0b000011, 0b000011, 0b011111, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b011101, 0, 0}, // b
			{0, 0, 0, 0b11110, 0b00011, 0b00011, 0b00011, 0b00011, 0b00011, 0b11110, 0, 0}, // c
			{0b110000, 0b110000, 0b110000, 0b111110, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b101110, 0, 0}, // d
			{0, 0, 0, 0b011110, 0b110011, 0b110011, 0b111111, 0b000011, 0b000011, 0b111110, 0, 0}, // e
			{0b1100, 0b0110, 0b0110, 0b1111, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0, 0}, // f
			{0, 0, 0, 0b101110, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b111110, 0b110000, 0b011110}, // g
			{0b000011, 0b000011, 0b000011, 0b011111, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0, 0}, // h
			{0b11, 0b11, 0, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0, 0}, // i
			{0b110, 0b110, 0, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b110, 0b011}, // j
			{0b000011, 0b000011, 0b000011, 0b110011, 0b110011, 0b011011, 0b001111, 0b011011, 0b110011, 0b110011, 0, 0}, // k
			{0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0b11, 0, 0}, // l
			{0, 0, 0, 0b0111011101, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0, 0}, // m
			{0, 0, 0, 0b011101, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0, 0}, // n
			{0, 0, 0, 0b011110, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b011110, 0, 0}, // o
			{0, 0, 0, 0b011101, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b011111, 0b000011, 0b000011}, // p
			{0, 0, 0, 0b101110, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b111110, 0b110000, 0b110000}, // q
			{0, 0, 0, 0b1101, 0b1111, 0b0011, 0b0011, 0b0011, 0b0011, 0b0011, 0, 0}, // r
			{0, 0, 0, 0b11110, 0b00011, 0b00111, 0b01110, 0b11100, 0b11000, 0b01111, 0, 0}, // s
			{0, 0b0100, 0b0110, 0b1111, 0b0110, 0b0110, 0b0110, 0b0110, 0b0110, 0b1100, 0, 0}, // t
			{0, 0, 0, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b110011, 0b101110, 0, 0}, // u
			{0, 0, 0, 0b110011, 0b110011, 0b110011, 0b110011, 0b010010, 0b011110, 0b001100, 0, 0}, // v
			{0, 0, 0, 0b1100110011, 0b1100110011, 0b1100110011, 0b1100110011, 0b0100110010, 0b0111111110, 0b0011001100, 0, 0}, // w
			{0, 0, 0, 0b110011, 0b110011, 0b011110, 0b001100, 0b011110, 0b110011, 0b110011, 0, 0}, // x
			{0, 0, 0, 0b110011, 0b110011, 0b110011, 0b110110, 0b010110, 0b011100, 0b001100, 0b000111, 0b000011, 0, 0}, // y
			{0, 0, 0, 0b111111, 0b110000, 0b011000, 0b001100, 0b000110, 0b000011, 0b111111, 0, 0}, // z
	};
	private static final String FONT_CHARS = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz";
	private static final int HEIGHT = 12;
	public static final NokiaFontSmall FONT = new NokiaFontSmall(false);

	public NokiaFontSmall() {
		this(true);
	}

	private NokiaFontSmall(boolean malleable) {
//		for (int[] aint : fontData) {
//			if (aint.length!=HEIGHT) System.out.println()
//		}

		this.setChar(' ', new CharacterSprite(2, HEIGHT, new boolean[2 * HEIGHT]));
		char[] charArray = FONT_CHARS.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char ch = charArray[i];
//				if (i >= 32 && i < 32 + FONT_CHARS.length()) {
//					ch = FONT_CHARS.charAt(i - 32);
//				}

			if (ch == 32) {
				this.setChar(ch, new CharacterSprite(2, HEIGHT, new boolean[2 * HEIGHT]));
			} else {
				int[] rows = fontData[i];

//				if (rows.length != HEIGHT) throw new IllegalStateException("Malformed font data: " + ch);

				int width = getCharWidthInitial(ch);
				int r;

				if (width == -1) {
					width = 0;
					for (int data = 0; data < HEIGHT; ++data) {
						for (r = 0; r < HEIGHT; ++r) {
							if ((rows[data] & 1 << r) != 0 && r > width) {
								width = r;
							}
						}
					}

					++width;
				}

				boolean[] var9 = new boolean[width * HEIGHT];

				for (r = 0; r < HEIGHT; ++r) {
					for (int c = 0; c < width; ++c) {
						var9[r * width + c] = (rows[r] & 1 << c) != 0;
					}
				}

				this.setChar(ch, new CharacterSprite(width, HEIGHT, var9));
			}
		}

		this.malleable = malleable;
	}

	private int getCharWidthInitial(char c) {
		switch (c) {
			case ' ':
				return 2;
			case '@':
				return 9;
		}

		return -1;
	}
}