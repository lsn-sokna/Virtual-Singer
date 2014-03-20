// エターナルフォースブリサンプラー ジェントリー・ウィープス　〜相手は死ぬ，俺も死ぬ〜
// ネタではじめたWORLD版UTAU合成エンジンです．WORLD 0.0.4をガンガン変えているので，
// このプログラムはWORLDとは別物だと思ったほうが良いです．
// プラチナの数字は千分率での純度を表していて，850以上がプラチナと認められる．
// よってPt100というのは，プラチナとはいえない別の何か（本プログラムにおける新機能）です．
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include <windows.h>

#include "world.h"
#include "wavread.h"

#include <math.h>

// 12引数のうち
// 1 入力ファイル（OK）
// 2 出力ファイル（OK）
// 3 音階（OK）
// 4 タイムパーセント
// 5 フラグ（無視）(OK)
// 6 オフセット
// 7 長さ調整
// 8 前半の固定部分
// 9 最後の固定部分
// 10 ボリューム (OK)
// 11 モジュレーション (OK)
// 12 ピッチベンド

// 分析シフト量 [msec]
#define FRAMEPERIOD 2.0

#pragma comment(lib, "winmm.lib")

void createFinalPitch2(double *f0, int tLen, double *pitchBend, int bLen, int fs, int tempo)
{
	int i,j;
	double *time1, *time2, *pitch;
	time1 = (double *)malloc(sizeof(double) * tLen);
	time2 = (double *)malloc(sizeof(double) * bLen);
	pitch = (double *)malloc(sizeof(double) * tLen);

	for(i = 0;i < tLen;i++) time1[i] = (double)i * FRAMEPERIOD;
	for(i = 0;i < bLen;i++) time2[i] = ((((double)i)/96.0)* (60.0/((double)(tempo))))*1000.0;

/*	interp1(time2, pitchBend, bLen, time1, tLen, pitch);*/
	for (i=0,j=0; i<tLen; i++) {
		if (j>=bLen) {
			pitch[i]=1.0;
			continue;
		}
		while (j < bLen && time1[i] > time2[j]) {
			j++;
		}
		if (j>=bLen) {
			pitch[i]=1.0;
			continue;
		}
		pitch[i]=pitchBend[j];
	}

//	for(i = 0;i < tLen;i++) f0[i] *= pitch[i];
	for(i = 0;i < tLen;i++) f0[i] *= pitch[i];

	for(i = 0;i < tLen;i+=10)
	{
//		printf("%f\n", pitch[i]);
	}

	free(time1); free(time2); free(pitch);
}

int base64decoderForUtau(char x, char y)
{
	int ans1, ans2, ans;

	if(x=='+') ans1 = 62;
	if(x=='/') ans1 = 63;
	if(x>='0' && x <= '9') ans1 = x+4;
	if(x>='A' && x <= 'Z') ans1 = x-65;
	if(x>='a' && x <= 'z') ans1 = x-71;

	if(y=='+') ans2 = 62;
	if(y=='/') ans2 = 63;
	if(y>='0' && y <= '9') ans2 = y+4;
	if(y>='A' && y <= 'Z') ans2 = y-65;
	if(y>='a' && y <= 'z') ans2 = y-71;

	ans = (ans1<<6) | ans2;
	if(ans >= 2048) ans -= 4096;
	return ans;
}

int getF0Contour(char *input, double *output)
{
	int i, j, count, length;
	i = 0;
	count = 0;
	double tmp;

	tmp = 0.0;
	while(input[i] != '\0')
	{
		if(input[i] == '#')
		{ // 別作業にいってらっしゃい
			length = 0;
			for(j = i+1;input[j]!='#';j++)
			{
				length = length*10 + input[j]-'0';
			}
			i = j+1;
			for(j = 0;j < length;j++)
			{
				if (output) {
					output[count++] = tmp;
				} else {
					count++;
				}
			}
		}
		else
		{
			tmp = pow(2.0, (double)base64decoderForUtau(input[i], input[i+1]) / 1200.0);
			if (output) {
				output[count++] = tmp;
			} else {
				count++;
			}
			i+=2;
		}
	}

	return count;
}

void equalizingPicth(double *f0, int tLen, char *scaleParam, int modulationParam)
{
	int i;
	// まず平均値を調べる．
	double averageF0;
	int count;
	int rangeSt, rangeEd;
	double modulation;

	modulation = (double)modulationParam / 100.0;

	averageF0 = 0;
	count = 0;
	rangeSt = max(0, tLen/2 - 50);
	rangeEd = min(tLen-1, tLen/2 + 49);

	for(i = rangeSt;i < rangeEd;i++)
	{
		if(f0[i] != 0.0)
		{
			averageF0 = averageF0 + f0[i];
			count++;
		}
	}
	averageF0 /= (double)count;

	int scale;
	int octave;
	double targetF0;
	int bias = 0;

	// 目標とする音階の同定
	if(scaleParam[1] == '#') bias = 1;

	switch(scaleParam[0])
	{
	case 'C':
		scale = -9+bias;
		break;
	case 'D':
		scale = -7+bias;
		break;
	case 'E':
		scale = -5;
		break;
	case 'F':
		scale = -4+bias;
		break;
	case 'G':
		scale = -2+bias;
		break;
	case 'A':
		scale = bias;
		break;
	case 'B':
		scale = 2;
		break;
	}
	octave = scaleParam[1+bias]-'0' - 4;
	targetF0 = 440 * pow(2.0,(double)octave) * pow(2.0, (double)scale/12.0);

	double tmp;
	for(i = 0;i < tLen;i++)
	{
		if(f0[i] != 0.0)
		{
			tmp = (f0[i]-averageF0)*modulation + averageF0;
			f0[i] = tmp * targetF0 / averageF0;
		}
	}
}


int stretchTime(double *f0, int tLen, int fftl, double **residualSpecgram,
				 double *f02, int tLen2, double **residualSpecgram2, int st, int ed, int loop)
{
	int i, j, k, count;

	count = 0;
	// 前半
	for(i = 0;i < st;i++)
	{
		if(count >= tLen2) break;
		f02[count] = f0[i];
		for(j = 0;j <= fftl/2;j++) residualSpecgram2[count][j] = residualSpecgram[i][j];
		count++;
	}
	for(i = 0;i < loop;i++)
	{
		for(j = st;j < ed;j++)
		{
			if(count >= tLen2) break;
			f02[count] = f0[j];
			for(k = 0;k < fftl/2;k++) residualSpecgram2[count][k] = residualSpecgram[j][k];
			count++;
//	printf("%d %d %d\n", tLen2, count, loop);
		}
		for(j = ed;j > st;j--)
		{
			if(count >= tLen2) break;
			f02[count] = f0[j];
			for(k = 0;k < fftl/2;k++) residualSpecgram2[count][k] = residualSpecgram[j][k];
			count++;
//	printf("%d %d %d\n", tLen2, count, loop);
		}
	}
	for(i = st;i < tLen;i++)
	{
		if(count >= tLen2) break;
		f02[count] = f0[i];
		for(j = 0;j < fftl/2;j++) residualSpecgram2[count][j] = residualSpecgram[i][j];
		count++;
	}
	return count;
}

int main(int argc, char *argv[])
{
	int i;

	double *x,*x_cut,*f0,*t,*y;
	double **residualSpecgram;
	int fftl;

	int signalLen;
	int tLen;
	
	int offset,blank;
	int offsetSample, lengthSample;

	if(argc < 3) 
	{
		printf("error: 引数の数が不正です．\n");
		return 0;
	}

	/*
	printf("argc:%d\n", argc);
	for(i = 0;i < argc;i++)
		printf("%d:%s\n",i, argv[i]);
	//*/

	FILE *fp;

	int fs, nbit;
	x = wavread(argv[1], &fs, &nbit, &signalLen);
	if(x == NULL)
	{
		printf("error: 指定されたファイルは存在しません．\n");
		return 0;
	}

	printf("File information\n");
	printf("Sampling : %d Hz %d Bit\n", fs, nbit);
	printf("Length %d [sample]\n", signalLen);
	printf("Length %f [sec]\n", (double)signalLen/(double)fs);
	
	// Cut x by offset and blank
	offset = atoi(argv[6]);
	offsetSample = offset*fs/1000;
	blank = atoi(argv[9]);
	if(blank < 0) // 負の場合はoffsetからの距離
	{
		lengthSample = (-blank)*fs/1000;
	} else {
		lengthSample = signalLen - offsetSample - (blank*fs/1000);
	}
	if (lengthSample <= 0) {
		printf("Error: offset passes blank\n");
		exit(0);
	}
	printf ("lengthSample: %d\n",lengthSample);
	x_cut = (double *)malloc(sizeof(double)*lengthSample);
	for (i=0; i<lengthSample ; i++) {
		if (i+offsetSample < signalLen) {
			x_cut[i] = x[i+offsetSample];
		} else {
			x_cut[i] = 0.0;
		}
	}
	// F0は何サンプル分あるかを事前に計算する．
	tLen = getSamplesForDIO(fs, lengthSample, FRAMEPERIOD);
	printf ("tLen: %d\n",tLen);
	f0 = (double *)malloc(sizeof(double)*tLen);
	t  = (double *)malloc(sizeof(double)*tLen);
	// f0 estimation by DIO
	DWORD elapsedTime;

	printf("\nAnalysis\n");
	elapsedTime = timeGetTime();
	dio(x_cut, lengthSample, fs, FRAMEPERIOD, t, f0);
	printf("DIO: %d [msec]\n", timeGetTime() - elapsedTime);

	fftl = getFFTLengthForStar(fs);

	residualSpecgram	= (double **)malloc(sizeof(double *) * tLen);
	for(i = 0;i < tLen;i++) residualSpecgram[i] = (double *)malloc(sizeof(double) * (fftl/2+1));

	// 非周期性指標の分析
	elapsedTime = timeGetTime();
	pt100(x_cut, lengthSample, fs, t, f0, residualSpecgram);
	printf("PLATINUM: %d [msec]\n", timeGetTime() - elapsedTime);

	// 時間長の伸縮
	int lengthMsec, stLengthMsec, inputLengthMsec;
	double ratio;
	inputLengthMsec = (int)(tLen*FRAMEPERIOD);
	lengthMsec = atoi(argv[7]);
	stLengthMsec = atoi(argv[8]);

	int loop;
	loop = (lengthMsec-stLengthMsec)/(inputLengthMsec-stLengthMsec);
	ratio = (double)(lengthMsec) / (double)(inputLengthMsec - stLengthMsec);

	// 制御パラメタのメモリ確保
	double *fixedF0;
	double **fixedResidualSpecgram;
	int tLen2;
//	tLen2 = (int)(0.5+(double)(lengthMsec+offset)/FRAMEPERIOD); //ここを修正
	tLen2 = (int)(0.5+(double)(lengthMsec+stLengthMsec)/FRAMEPERIOD);

	fixedF0					= (double *) malloc(sizeof(double)   * tLen2);
	fixedResidualSpecgram	= (double **)malloc(sizeof(double *) * tLen2);
	for(i = 0;i < tLen2;i++) fixedResidualSpecgram[i]	= (double *)malloc(sizeof(double) * (fftl/2+1));

	// 最終波形のメモリ確保
	int signalLen2;
	signalLen2 = (int)((lengthMsec+stLengthMsec)/1000.0*(double)fs);
	y  = (double *)malloc(sizeof(double)*signalLen2);
	if (!y) {
		printf ("Error: y malloc() NULL\n");
	}
	for(i = 0;i < signalLen2;i++) y[i] = 0.0;
//	printf("length:%d, %f\n",signalLen2, (double)signalLen2/(double)fs*1000.0);
//	printf("%d, %d, %d\n",lengthMsec, offset, fs);


	// 合成の前にF0の操作 (引数)
	equalizingPicth(f0, tLen, argv[3], atoi(argv[11]) );
//	stretchTime(f0, tLen, fftl, residualSpecgram, 
//				 fixedF0, tLen2, fixedResidualSpecgram, stLengthMsec/(int)FRAMEPERIOD, ratio);
	int st, ed;
	int tempo;
	int pitchType;
	st = stLengthMsec+(inputLengthMsec-stLengthMsec)/3;
	ed = stLengthMsec+2*(inputLengthMsec-stLengthMsec)/3;

	tLen2 = stretchTime(f0, tLen, fftl, residualSpecgram, 
				 fixedF0, tLen2, fixedResidualSpecgram, st/(int)FRAMEPERIOD, ed/(int)FRAMEPERIOD, loop);
	pitchType=0;
	if (argc >= 13 && argv[12][0]=='!') {
		tempo = atoi(&(argv[12][1]));
		pitchType=0;
	} else if (argc >= 13 && strchr(argv[12],'Q') != NULL) {
	        char *c;
	        pitchType=1;
		c = strchr(argv[12],'Q');
		c++;
		tempo = atoi(c);
	} else if (argc >= 13) {
		tempo = atoi(argv[12]);
	} else {
		tempo = 120;
	}
	printf ("tempo: %d\n",tempo);

	// ピッチベンドの取得
	double *pitchBend=NULL;
	int bLen=0;
	if (pitchType==0) {
	  if (argc >= 14) {
	    bLen = getF0Contour(argv[13],NULL);
	    pitchBend = (double *)malloc(sizeof(double) * bLen);
	    bLen = getF0Contour(argv[13], pitchBend);
	  } else {
	    bLen = 3;
	    pitchBend = (double *)malloc(sizeof(double) * bLen);
	    pitchBend[0]=1.0;
	    pitchBend[1]=1.0;
	    pitchBend[2]=1.0;
	  }
	} else if (pitchType==1) {
	  int i,j;
	  char arg1[100];
	  bLen = argc - 12;
	  pitchBend = (double *)malloc(sizeof(double) * bLen);
	  for (i=0; i<bLen; i++) {
	    memset(arg1,0,sizeof(arg1));
	    for (j=0; j+1<sizeof(arg1) && argv[i+12][j] != '\0'; j++) {
	      if (isdigit(argv[i+12][j]) || argv[i+12][j]=='.' || argv[i+12][j]=='+' || argv[i+12][j]=='-') {
		arg1[j]=argv[i+12][j];
	      } else {
		break;
	      }
	    }
	    sscanf(arg1,"%lf",&(pitchBend[i]));
	    pitchBend[i] = pow(2.0, pitchBend[i]/1200.0);
	  }
	}
	createFinalPitch2(fixedF0, tLen2, pitchBend, bLen, fs, tempo);

	// 合成
	printf("\nSynthesis\n");
	elapsedTime = timeGetTime();
	synthesisPt100(fixedF0, tLen2, fixedResidualSpecgram, fftl, FRAMEPERIOD, fs, y, signalLen2);
	printf("WORLD: %d [msec]\n", timeGetTime() - elapsedTime);
	// オフセットの設定
	//offset = (int)((double)(offset+endOffset)/1000.0*(double)fs);
	//signalLen2 = (int)((lengthMsec)/1000.0*(double)fs);
//	signalLen2 -= offset;
	//for(i = 0;i < signalLen2;i++) y[i] = y[i+offset];

//	printf("%d\n", signalLen2);
//	printf("%f %d\n", (double)signalLen2/(double)fs*1000, offset);
//	getch();


	// ファイルの書き出し (内容には関係ないよ)
	char header[44];
	short *output;
	double maxAmp;
	output = (short *)malloc(sizeof(short) * signalLen2);
 
	// 振幅の正規化
	maxAmp = 0.0;
	double volume;
	volume = (double)atoi(argv[10]) / 100.0;
	printf ("volume: %lf\n",volume);
	for(i = 0;i < signalLen2;i++) maxAmp = (maxAmp < fabs(y[i])) ? fabs(y[i]) : maxAmp;
	if (maxAmp<=0.0) maxAmp=1.0;
	printf ("maxAmp: %lf\n",maxAmp);
	for(i = 0;i < signalLen2;i++) output[i] = (short)(32768.0*(y[i]*0.5 * volume/maxAmp));

	fp = fopen(argv[1], "rb");
	fread(header, sizeof(char), 44, fp);
	fclose(fp);

	fp = fopen(argv[2],"wb");
	fwrite(header, sizeof(char), 44, fp);
	fwrite(output, sizeof(short), signalLen2, fp);
	fseek(fp, 40, SEEK_SET);
	signalLen2*=2;
	fwrite(&signalLen2, sizeof(int), 1, fp);
	fclose(fp);
	free(output);

	free(pitchBend);
	free(x); free(t); free(f0); free(fixedF0); free(y);
	for(i = 0;i < tLen;i++)
	{
		free(residualSpecgram[i]);
	}
	for(i = 0;i < tLen2;i++)
	{
		free(fixedResidualSpecgram[i]);
	}
	free(fixedResidualSpecgram);
	free(residualSpecgram); 

	printf("complete.\n");
	return 0;
}
