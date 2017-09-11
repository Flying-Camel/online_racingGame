#include <stdio.h>

#include <stdlib.h>

#include <string.h>

#include <unistd.h>

#include <signal.h>

#include <sys/wait.h>

#include <arpa/inet.h>

#include <sys/socket.h>

#include <sys/shm.h>



#include <time.h>

#include <fcntl.h>



#define BUF_SIZE 30

#define SHARED_KEY_NAME 2000

#define SHARED_KEY_CNUM 1235

#define SHARED_KEY_RNUM 1236

#define SHARED_KEY_LNUM 1237



//사용자들의 이름을 갖는 구조체

struct NAME{

	char names[3][10];

};

//사용자들의 위치와 점수를 갖는 구조체

struct location{

	int loc[3][2];

	int score[3];

};



void error_handling(char *message);

void read_childproc(int sig);

void childProcess();

void ReadWrite();

void idCheck();

void readyCheck();

void initName();

void initInfo();

void shareName();

void shareCnum();

void shareLoc();

void start();

void Log_clnt(char* to, char* msg);

void Log_serv(char* msg);





int serv_sock, clnt_sock;



void *shm = (void *)0;

int *clntNum;

int *readyNum;

int shm_id;

int myPlayer;



struct NAME *name;

struct location *ploc;



int main(int argc, char *argv[])

{

	struct sockaddr_in serv_adr, clnt_adr;



	pid_t pid;

	struct sigaction act;

	socklen_t adr_sz;

	int str_len, state;

	char buf[BUF_SIZE];



	//공유메모리를 생성하는 부분

	//공유메모리 생성 후 초기화해준다.

	shareName();

	shareCnum();

	shareLoc();

	initName();

	initInfo();



	/*

	   if(argc!=2) {

	   printf("Usage : %s <port>\n", argv[0]);

	   exit(1);

	   }

	 */



	//TCP 접속

	act.sa_handler=read_childproc;

	sigemptyset(&act.sa_mask);

	act.sa_flags=0;

	state=sigaction(SIGCHLD, &act, 0);

	serv_sock=socket(PF_INET, SOCK_STREAM, 0);

	memset(&serv_adr, 0, sizeof(serv_adr));

	serv_adr.sin_family=AF_INET;

	serv_adr.sin_addr.s_addr=htonl(INADDR_ANY);

	//   serv_adr.sin_port=htons(atoi(argv[1]));

	serv_adr.sin_port=htons(9999);



	if(bind(serv_sock, (struct sockaddr*) &serv_adr, sizeof(serv_adr))==-1)

		error_handling("bind() error");

	if(listen(serv_sock, 5)==-1)

		error_handling("listen() error");





	printf("Wating for Client...\n");



	while(1)

	{

		adr_sz=sizeof(clnt_adr);

		clnt_sock=accept(serv_sock, (struct sockaddr*)&clnt_adr, &adr_sz);



		if(clnt_sock==-1)

			continue;

		else{

			puts("new client connected...");

		}



		pid=fork();

		if(pid==-1)

		{

			close(clnt_sock);

			continue;

		}

		if(pid==0){

			//자식의 경우 리스닝 소켓을 닫고

			//자식 process를 실행한다.

			close(serv_sock);

			childProcess();

		}

		else{

			//부모의 경우 커넥티드 소켓을 닫고 

			//새로운 연결을 준비

			close(clnt_sock);

		}

	}

	close(serv_sock);

	return 0;

}



void childProcess(){



	//자식프로세스에서 부모와 통신하기위한

	//공유메모리 생성

	shareName();

	shareCnum();



	//클라이언트가 처음 접속했을 때 아이디를 체크해준다.

	idCheck();

	//아이디 체크 후 

	//게임이 끝났을 때 다시 재시작하기위한

	//while문 실행

	while(1){

		//레디 체크하고

		readyCheck();

		//클라이언트에서 start 보내주고

		start();

		//클라이언트와 통신한다

		ReadWrite();



		//      Log_serv("게임종료");//////////////////종료



	}

	exit(0);

}

void ReadWrite(){

	char *token = NULL;

	char buf[20];

	char buf2[50];

	int i,j;



	while(1){

		memset(buf,0,20);

		memset(buf2,0,50);



		read(clnt_sock,buf,20);



		//읽어온 정보를 LOC와END로 나누어 처리한다

		//정보는 정보카테고리|정보 형식으로 들어온다

		//만약 둘 다 아니라면 오류문구 띄우고 프로그램 종료

		token = strtok(buf,"|");

		if(!strcmp(buf,"LOC")){



			//LOC일 경우 LOC|x좌표|y좌표|SCORE 형식으로 읽는다

			token = strtok(NULL,"|");

			ploc->loc[myPlayer][0] = atoi(token);



			token = strtok(NULL,"|");

			ploc->loc[myPlayer][1] = atoi(token);

			token = strtok(NULL,"\n");

			ploc->score[myPlayer] = atoi(token);



			//모두 읽어서 

			//LOC|p1x|p1y|p2x|p2y|p3x|p3y|p1점수|p2점수|p3점수|

			//형식으로 write한다.

			strcpy(buf2,"LOC|");

			char tmp[5]={0};

			for(i=0;i<3;i++)

				for(j=0;j<2;j++){

					memset(tmp,0,5);

					sprintf(tmp,"%d|",ploc->loc[i][j]);

					strcat(buf2,tmp);

				}

			for(i=0;i<3;i++){

				memset(tmp,0,5);

				sprintf(tmp,"%d|",ploc->score[i]);

				strcat(buf2,tmp);

			}

			write(clnt_sock,buf2,50);



		}

		//END일경우 게임 종료

		else if(!strcmp(buf,"END")){

			strcpy(buf2,"END|");

			write(clnt_sock,buf2,50);

			printf("player%d 게임종료\n",myPlayer+1);



			ploc->loc[myPlayer][0]=-1;

			ploc->loc[myPlayer][1]=-1;



			break;

		}

	}

}



void start(){

	int i;



	//클라이언트가 모두 준비했다면 게임을 시작한다.

	//클라이언트수와 레디누른 수가 같으면 시작.

	while(1){

		if((*clntNum) ==  (*readyNum)){



			if(readyNum!=0)

				Log_serv("게임시작");////////로그



			//게임 시작전에 닉네임들을 한번 더 보내준다.

			char nameTmp[40]={0};

			//name 리스트를 보내준다.

			//NAME|name1|name2|name3| 의 형식으로 보낸다.

			strcat(nameTmp,"NAME|");

			for(i=0;i<3;i++){

				strcat(nameTmp,name->names[i]);

				strcat(nameTmp,"|");

			}

			write(clnt_sock,nameTmp,40);

			initInfo();



			sleep(3);



			printf("Player%d 게임 시작!\n",myPlayer+1);

			write(clnt_sock,"START|\0",10);





			(*readyNum) = 0;

			break;

		}

	}

}



void readyCheck(){

	char buf[15] = {0,};

	char *token=NULL;

	int isReady=0;

	int i;



	//클라이언트가 준비하는 과정이다.

	while(1){

		read(clnt_sock,buf,15);



		//클라이언트가 보낸 정보를 QUIT READY START로 나누어 처리한다

		token = strtok(buf,"|");

		if(!strcmp(buf,"QUIT")){



			token = strtok(NULL,"\n");



			Log_clnt(name->names[myPlayer],"종료"); ///////////////로그



			//현재 내 아이디를 아이디배열에서 지운다

			for(i=0;i<3;i++)

				if(!strcmp(token,name->names[i])){

					memset(name->names[i],0,sizeof(name->names[i]));

					break;

				}



			printf("======================================\n");

			for(i=0;i<3;i++)

				printf("접속된 클라이언트 : %s\n",name->names[i]);

			printf("======================================\n");



			//클라이언트 수를 하나 줄이고, 커넥티드 소켓을 닫는다

			(*clntNum) = (*clntNum) - 1;

			close(clnt_sock);

			exit(0);

		}

		else if(!strcmp(buf,"READY")){

			token = strtok(NULL,"\n");



			if(!strcmp(token,"1")){

				(*readyNum) = (*readyNum) + 1;

				printf("클라이언트 준비완료 (%d/%d)\n",(*readyNum),(*clntNum));



				Log_clnt(name->names[i],"준비완료"); /////////로그

				break;

			}

			if(!strcmp(token,"0")){

				printf("클라이언트 준비해재\n");

				(*readyNum--);

			}

		}

		else if(!strcmp(buf,"START")){

			break;

		}

		else    

			error_handling("치명적인 오류가 발생했습니다.\n");

	}

}



void idCheck(){



	char *token=NULL;

	int i;

	int check=0;



	//가장 처음에 아이디를 체크하는 부분

	while(1){

		char buf[30]={0,};

		int len = read(clnt_sock,buf,30);



		token = strtok(buf,"|");



		//사용자는 NAME|아이디 형식으로 보낸다.

		if(!strcmp(buf,"NAME")){

			token = strtok(NULL,"\n");



			for(i=0;i<3;i++){

				if(!strcmp(name->names[i],token))

					check=1;

			}

			//이름 중복확인 중복이라면

			if(check==1){

				write(clnt_sock,"DUP|0",5);

				check=0;

				continue;



			}

			//중복이 아니라면 

			else{

				for(i=0;i<3;i++)

					if(name->names[i][0]==0)

						break;



				write(clnt_sock,"DUP|1",5);

				strcpy(name->names[i],token);



				Log_clnt(name->names[i],"접속"); /////////로그



				//내 플레이어 넘버 저장

				myPlayer = i;



				//플레이어 넘버를 보내준다.

				//넘버는 NUM|넘버 형식으로 보낸다

				char tmp[5] = {"NUM|"}, tmp2[5]={0};

				i=i+1;

				sprintf(tmp2,"%d",i);

				strcat(tmp,tmp2);

				write(clnt_sock,tmp,10);



				char nameTmp[90]={0};



				//현재까지 접속한 

				//name 리스트를 보내준다.

				strcat(nameTmp,"NAME|");

				for(i=0;i<3;i++){

					strcat(nameTmp,name->names[i]);

					strcat(nameTmp,"|");

				}



				write(clnt_sock,nameTmp,90);



				//클라이언트 숫자 늘려준다

				(*clntNum) = (*clntNum) + 1;

				printf("현재 클라이언트 수 : %d\n",(*clntNum));

				break;

			}

		}

		else{

			printf("\n클라이언트 전송오류\n");

		}

	}

	printf("======================================\n");

	for(i=0;i<3;i++)

		printf("접속된 클라이언트 : %s\n",name->names[i]);

	printf("======================================\n");

}



void read_childproc(int sig)

{

	pid_t pid;

	int status;

	pid=waitpid(-1, &status, WNOHANG);

	printf("removed proc id: %d \n", pid);

}



void error_handling(char *message)

{

	fputs(message, stderr);

	fputc('\n', stderr);

	exit(1);

}



void initInfo(){

	int i,j;

	for(i=0;i<3;i++){

		for(j=0;j<2;j++)

			ploc->loc[i][j]=-1;

		ploc->score[i]=0;

	}

}



void initName(){



	memset(name->names[0],0,sizeof(name->names[0])); //이름배열 초기화

	memset(name->names[1],0,sizeof(name->names[1])); //이름배열 초기화

	memset(name->names[2],0,sizeof(name->names[2])); //이름배열 초기화



	(*clntNum) = 0;

	(*readyNum) = 0;



}



void shareLoc(){



	shm_id = shmget((key_t)SHARED_KEY_LNUM, sizeof(name), 0666|IPC_CREAT);

	if(shm_id == -1)

		error_handling("shmget 오류");

	shm = shmat(shm_id, NULL,0);

	if(shm == (void*)-1)

		error_handling("shmat 오류");

	ploc  = (struct location*)shm;



}



void shareName(){



	shm_id = shmget((key_t)SHARED_KEY_NAME, sizeof(name), 0666|IPC_CREAT);

	if(shm_id == -1)

		error_handling("shmget 오류");

	shm = shmat(shm_id, NULL,0);

	if(shm == (void*)-1)

		error_handling("shmat 오류");

	name = (struct NAME*)shm;



}



void shareCnum(){



	shm_id = shmget((key_t)SHARED_KEY_CNUM, sizeof(int), 0666|IPC_CREAT);

	if(shm_id == -1)

		error_handling("shmget 오류");

	shm = shmat(shm_id, NULL,0);

	if(shm == (void*)-1)

		error_handling("shmat 오류");

	clntNum = (int*)shm;



	shm_id = shmget((key_t)SHARED_KEY_RNUM, sizeof(int), 0666|IPC_CREAT);

	if(shm_id == -1)

		error_handling("shmget 오류");

	shm = shmat(shm_id, NULL,0);

	if(shm == (void*)-1)

		error_handling("shmat 오류");

	readyNum = (int*)shm;

}

void Log_clnt(char* id, char* msg)



{

	time_t now;

	struct tm* now_t;

	char message[255];

	int fd;



	now = time(&now);

	now_t = localtime(&now);



	if((fd = open("log.txt", O_WRONLY | O_CREAT | O_APPEND, 0644)) < 0){ 

		printf( "log open error\n");

		exit(1);

	}



	sprintf(message , "client[%s] : %s ... 시간 : %s",id,msg, asctime(now_t));

	write(fd, message, strlen(message));

	close(fd);  

}   



void Log_serv(char *msg)

{

	time_t now;

	struct tm* now_t;

	char message[255];

	int fd;

	now = time(&now);

	now_t = localtime(&now);



	if((fd = open("log.txt", O_WRONLY | O_CREAT | O_APPEND, 0644)) < 0){

		printf( "log open error\n");

		exit(1);

	}



	sprintf(message , "[%s] ... 시간 : %s",msg, asctime(now_t));

	write(fd, message, strlen(message));

	close(fd);  





}
