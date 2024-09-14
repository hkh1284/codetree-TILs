import java.util.*;
import java.io.*;

/*
문제 풀이 아이디어
1. 반복문으로 1~k번의 정령 선택
2. 골렘을 최대한 남쪽으로 이동시키기
3. 골렘의 일부가 맵을 벗어나는지 확인
    3-1. 벗어나면, 맵을 정리하고(clear()) 다음 정령으로 넘어가기
    3-2. 벗어나지 않으면, 정령을 가장 남쪽으로 이동시키기(moveFairy())
4. 정령의 행번호만큼 answer 증가


- (mapR+3) * mapC 크기의 map에 골렘이 위치한 자리를 주인정령 번호로 표시
- 각 정령이 소유한 골렘의 출구를 Exit[] exitList로 표시
- 각 정령의 위치를 fairy[] fairyList로 표시
*/

/*
골렘을 최대한 남쪽으로 이동시키기 moveGolem()
0. 아래의 동작 반복
1. 남쪽으로 이동가능한지 체크 checkSouth(nowR, nowC)
2. 가능하면 남쪽으로 이동
3. 불가능하면 서쪽으로 이동 후 남쪽으로 이동가능한지 체크 checkWest(nowR, nowC) -> checkSouth(nowR, nextC-1)
4. 가능하면 서쪽으로 이동 후 남쪽으로 이동
5. 불가능하면 동쪽으로 이동 후 남쪽으로 이동가능한지 체크 checkEast(nowR, nowC) -> checkSouth(nowR, nextC+1)
6. 가능하면 동쪽으로 이동 후 남쪽으로 이동
7. 불가능하면 반복문 탈출

- 정령 위치를 기준으로 이동시키기
*/

/*
정령을 가장 남쪽으로 이동시키기 moveFairy()
0. 가장 남쪽 행일 때의 번호를 저장할 변수 result
1. bfs로 상하좌우 탐색
2. visited[][]의 값이 현재 fnum가 아니면 미방문. 현재 fnum이면 방문.
3. 현재 위치의 행이 result보다 클 때, result 갱신
4. 현재 위치가 x, y일 때 Exit[map[x][y]].x, Exit[map[x][y]].y와 같다면 상하좌우에 map[x][y]와는 다른 값이라도 큐에 넣기
5. 현재 위치가 x, y일 때 Exit[map[x][y]].x, Exit[map[x][y]].y와 다르면 상하좌우에 map[x][y]와는 다른 값일 시 큐에 넣지 않기 
6. result-2 반환
*/


public class Main {
    static class Fairy{
        int row;
        int col;
        public Fairy(int row, int col){
            this.row=row;
            this.col=col;
        }
    }
    static class Exit{
        int row;
        int col;
        public Exit(){}
        public Exit(int row, int col){
            this.row=row;
            this.col=col;
        }
    }

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringBuilder sb = new StringBuilder();
    static StringTokenizer st;

    static int mapR, mapC, fairyNum, answer;
    static int[] dr = {-1, 0, 1, 0}; //북동남서
    static int[] dc = {0, 1, 0, -1};

    static Fairy[] fairyList;
    static int[] exitDirect;
    static Exit[] exitList;

    static Deque<Integer> deque;

    static int[][] map;
    static int[][] visited;

    static void inputTestcase() throws IOException {
        st = new StringTokenizer(br.readLine().trim());
        mapR = Integer.parseInt(st.nextToken());
        mapC = Integer.parseInt(st.nextToken());
        fairyNum = Integer.parseInt(st.nextToken());
        
        map = new int[mapR+3][mapC+1];
        visited = new int[mapR+3][mapC+1];
        

        fairyList = new Fairy[fairyNum+1];
        exitDirect = new int[fairyNum+1];
        exitList = new Exit[fairyNum+1];

        for(int fnum=1; fnum<=fairyNum; fnum++){
            st = new StringTokenizer(br.readLine().trim());
            Fairy newone = new Fairy(1, Integer.parseInt(st.nextToken()));
            fairyList[fnum] = newone;
            exitDirect[fnum]=Integer.parseInt(st.nextToken());
            exitList[fnum] = new Exit();
        }
    }

    public static void main(String[] args) throws IOException {
        //입력
        inputTestcase();

        //1. 반복문으로 1~fairyNum번의 정령 선택
        for(int fnum=1; fnum<=fairyNum; fnum++){
            //2. 골렘을 최대한 남쪽으로 이동시키기 moveGolem => 골렘이 맵을 벗어나면 true반환, 맵 안이면 false반환
            //3. 골렘의 일부가 맵을 벗어나는지 확인 
            boolean checkMapOut = moveGolem(fnum);

            //테스트코드
            //System.out.print("골렘이 맵밖으로 나갔는가?");
            // if(checkMapOut){System.out.println("yes");}
            // else{System.out.println("no");}

            //3-1. 벗어나면, 맵을 정리하고(clear()) 다음 정령으로 넘어가기
            if(checkMapOut){
                clear();
                continue;
            }
            //3-2. 벗어나지 않으면, 정령을 가장 남쪽으로 이동시키기(moveFairy()) => 최종 정령 위치 행번호만큼 answer증가
            answer+=moveFairy(fnum);
        }

        //출력
        System.out.println(answer);
    }

    static int moveFairy(int fnum){
        setExit(fnum);
        int result=0;
        //1. bfs로 상하좌우 탐색
        deque = new ArrayDeque<>();
        deque.addLast(fairyList[fnum].row);
        deque.addLast(fairyList[fnum].col);
        visited[fairyList[fnum].row][fairyList[fnum].col]=fnum;
        while(!deque.isEmpty()){
            int nowR = deque.removeFirst();
            int nowC = deque.removeFirst();
            //2. 현재 위치의 행이 result보다 클 때, result 갱신
            if(nowR>result){result=nowR;}
            //3. 현재 위치가 해당 골렘의 출구라면
            boolean exitFlag=false;
            if(nowR==exitList[map[nowR][nowC]].row && nowC==exitList[map[nowR][nowC]].col){
                exitFlag=true;
                //System.out.println("now is exit");
            }
            //4. 상하좌우 탐색
            for(int idx=0; idx<4; idx++){
                int nextR = nowR + dr[idx];
                int nextC = nowC + dc[idx];
                if(nextR<3 || nextR>mapR+2 || nextC<1 || nextC>mapC){continue;}
                if(visited[nextR][nextC]==fnum){continue;}
                //next위치의 맵번호가 now위치의 맵번호와 같다면
                if(map[nextR][nextC]==map[nowR][nowC]){
                    visited[nextR][nextC]=fnum;
                    deque.addLast(nextR);
                    deque.addLast(nextC);
                }
                //next위치의 맵번호가 0이 아니면서 now위치의 맵번호와 다르면
                if(map[nextR][nextC]!=0 && map[nextR][nextC]!=map[nowR][nowC]){
                    //현재 위치가 해당 골렘의 출구라면 큐에 넣기
                    if(exitFlag){
                        visited[nextR][nextC]=fnum;
                        deque.addLast(nextR);
                        deque.addLast(nextC);
                    }
                }
            }
        }

        //5. result-2 반환
        // System.out.println("정령이 최종적으로 위치한 행의 위치: "+result);
        // System.out.println();
        return result-2;
    }



    static boolean moveGolem(int fnum){
            int nowR = fairyList[fnum].row;
            int nowC = fairyList[fnum].col;
            //최대한 남쪽으로 이동
            while(true){
                //1. 남쪽으로 이동가능한지 체크 후, 가능하면 이동
                if(checkSouth(nowR, nowC)){
                    nowR++;
                    continue;
                }
                //2. 불가능하면 서쪽->남쪽으로 이동가능한지 체크 후, 가능하면 이동
                if(checkWest(nowR, nowC)){
                    exitDirect[fnum]=exitDirect[fnum]-1;
                    if(exitDirect[fnum]==-1){exitDirect[fnum]=3;}
                    setExit(fnum);
                    nowC--;
                    nowR++;
                    continue;
                }
                //3. 불가능하면 동쪽->남쪽으로 이동가능한지 체크 후, 가능하면 이동
                if(checkEast(nowR, nowC)){
                    exitDirect[fnum]=exitDirect[fnum]+1;
                    if(exitDirect[fnum]==4){exitDirect[fnum]=0;}
                    setExit(fnum);
                    nowC++;
                    nowR++;
                    continue;
                }
                //4. 불가능하면 변경된 정령의 위치를 fairyList에 저장 후, 무한루프 탈출
                fairyList[fnum].row = nowR;
                fairyList[fnum].col = nowC;
                break;
            }

            //맵에서 현재 정령의 위치와 상하좌우 위치에 현재 정령번호 표시
            map[nowR][nowC]=fnum;
            for(int idx=0; idx<4; idx++){
                int nextR=nowR+dr[idx];
                int nextC=nowC+dc[idx];
                map[nextR][nextC]=fnum;
            }
            

            //테스트코드
            // if(fnum>=5){
            //     for(int row=0; row<mapR+3; row++){
            //         for(int col=0; col<=mapC; col++){
            //             System.out.print(map[row][col]+" ");
            //         }
            //         System.out.println();
            //     }    
            // }
            
            // System.out.println("fairyList["+fnum+"].row: "+fairyList[fnum].row);
            // System.out.println("fairyList["+fnum+"].col: "+fairyList[fnum].col);

            //골렘이 맵을 벗어나면 true, 아니면 false 반환
            if(fairyList[fnum].row<=3 || fairyList[fnum].col<=1 || fairyList[fnum].col>=mapC){
                return true;
            }
            return false;
        }

    static boolean checkSouth(int nowR, int nowC){
        if(nowR+2>mapR+2){return false;}
        if(map[nowR+2][nowC]==0 && map[nowR+1][nowC-1]==0 && map[nowR+1][nowC+1]==0){return true;}
        return false;
    }

    static boolean checkWest(int nowR, int nowC){
        if(nowR+2>mapR+2 || nowC-2<1){return false;}
        if(map[nowR][nowC-2]==0 && map[nowR-1][nowC-1]==0 && map[nowR+1][nowC-1]==0 && map[nowR+1][nowC-2]==0 && map[nowR+2][nowC-1]==0){return true;}
        return false;
    }

    static boolean checkEast(int nowR, int nowC){
        if(nowR+2>mapR+2 || nowC+2>mapC){return false;}
        if(map[nowR][nowC+2]==0 && map[nowR-1][nowC+1]==0 && map[nowR+1][nowC+1]==0 && map[nowR+1][nowC+2]==0 && map[nowR+2][nowC+1]==0){return true;}
        return false;
    }




    static void clear(){
        for(int row=0; row<mapR+3; row++){
            for(int col=0; col<=mapC; col++){
                map[row][col]=0;
            }
        }
    }

    

    static void setExit(int fnum){
        //북
        if(exitDirect[fnum]==0){
            exitList[fnum].row=fairyList[fnum].row-1;
            exitList[fnum].col=fairyList[fnum].col;            
        }
        //동
        else if(exitDirect[fnum]==1){
            exitList[fnum].row=fairyList[fnum].row;
            exitList[fnum].col=fairyList[fnum].col+1;            
        }
        //남
        else if(exitDirect[fnum]==2){
            exitList[fnum].row=fairyList[fnum].row+1;
            exitList[fnum].col=fairyList[fnum].col;            
        }
        //서
        else if(exitDirect[fnum]==3){
            exitList[fnum].row=fairyList[fnum].row;
            exitList[fnum].col=fairyList[fnum].col-1;            
        }
    }

    

    

}