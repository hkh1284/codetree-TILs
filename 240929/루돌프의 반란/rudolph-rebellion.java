import java.io.*;
import java.util.*;

public class Main {
	static class Santa {
		int x;
		int y;
		
		public Santa(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	static class Distance implements Comparable<Distance> {
		int d;
		int x;
		int y;
		
		public Distance(int d, int x, int y) {
			this.d = d;
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Distance o) {
			if (this.d != o.d) {
				return this.d - o.d;
			}
			
			if (this.x != o.x) {
				return o.x - this.x;
			}
			
			return o.y - this.y;
		}
	}
	
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static StringBuilder sb = new StringBuilder();
    static StringTokenizer st;
    
    static int N, M, P, C, D;
    
    //루돌프 위치
    static int rx, ry;
    
    //상, 우, 하, 좌
    static int[] dx = {-1, 0, 1, 0};
	static int[] dy = {0, 1, 0, -1};
	
	static int[][] map; // 산타는 번호, 루돌프는 -1
	static Santa[] santa;
	
	static int[] stun; // 산타가 기절했는지 확인
	static boolean[] dead; // 산타가 죽었는지 확인
	static int[] score; // 산타가 얻은 점수 저장
	


	
	public static void main(String[] args) throws IOException {
		//입력
		inputTestcase();
		
		//게임 턴 수만큼 반복
		for(int turn=1; turn<=M; turn++) {
			//가장 가까운 산타의 위치
			int minX = 10000;
        	int minY = 10000;
        	//가장 가까운 산타의 번호
        	int minId = 0;
        	
        	//가장 가까운 산타 찾기
        	for (int i = 1; i <= P; i++) {
    			// 격자 밖을 벗어난 산타일 경우 다음으로 넘어감
    			if (dead[i]) {
    				continue;
    			}
    			
    			Distance min = new Distance((int) (Math.pow(minX - rx, 2) + Math.pow(minY - ry, 2)), minX, minY);
    			Distance cur = new Distance((int) (Math.pow(santa[i].x - rx, 2) + Math.pow(santa[i].y - ry, 2)), santa[i].x, santa[i].y);
    			
    			if (cur.compareTo(min) < 0) {
    				minX = cur.x;
    				minY = cur.y;
    				minId = i;
    			}
    		}
        	//System.out.println(minX + " " + minY + " " + minId);
        	//루돌프가 가장 가까운 산타를 향해 1칸 이동
        	if(minId!=0) {
        		moveRudolph(minX, minY, minId);
        	}
        	//산타들이 루돌프와 가장 가까운 방향으로 1칸 이동
        	moveSanta();
        	//생존한 산타의 점수 증가
        	giveSurvivalScore();
        	//기절한 산타들의 남은 기절 턴수 감소
        	decreaseStunTurn();
		}
		
		// 산타가 얻은 최종점수 출력
        for (int idx = 1; idx <= P; idx++) {
        	System.out.print(score[idx] + " ");
        }
	}
	
	
	
	static void moveRudolph(int x, int y, int id) {
		//루돌프의 이동 방향 정하기
		int moveX=0;
		int moveY=0;
		if(x>rx) {
			moveX=1;
		}else if(x<rx) {
			moveX=-1;
		}
		if (y > ry) {
			moveY = 1;
		} else if (y < ry) {
			moveY = -1;
		}
		
		//맵에서 기존의 루돌프 위치를 빈칸으로 변경
		map[rx][ry]=0;
		//루돌프 이동
		rx+=moveX;
		ry+=moveY;
		//루돌프가 움직여 산타와 충돌한 경우
		if(rx==x && ry==y) {
			//충돌한 산타가 이동할 위치 계산
			//루돌프가 이동해온 방향으로 C칸 움직인 위치 
			int firstX = x+moveX*C;
			int firstY = y+moveY*C;
			//
			int lastX = firstX;
			int lastY = firstY;
			//산타 기절
			stun[id]=2;
			//충돌한 산타가 이동할 위치가 맵 내부이고, 다른 산타가 있는 경우
			while(isRange(lastX, lastY) && map[lastX][lastY]>0) {
				//한칸씩 이동
				lastX+=moveX;
				lastY+=moveY;
			}
			//연쇄 충돌 마지막 위치부터 산타 이동
			while(!(lastX==firstX && lastY==firstY)) {
				//lastX,lastY에 있던 산타가 충돌 전의 위치 prevX,prevY
				int prevX = lastX-moveX;
				int prevY = lastY-moveY;
				//맵범위를 벗어나면 종료
				if(!isRange(prevX, prevY)) {
					break;
				}
				//맵범위 안이면 (prevX,prevY)의 값을 (lastX,lastY)로 옮기기
				int idx=map[prevX][prevY];
				
				if(isRange(lastX, lastY)) {
					map[lastX][lastY]=idx;
					santa[idx] = new Santa(lastX, lastY);
				}else {
					dead[idx]=true;
				}
				lastX=prevX;
				lastY=prevY;
			}
			//충돌한 산타 점수 증가
			score[id]+=C;
			if(isRange(firstX,firstY)) {
				map[firstX][firstY]=id;
				santa[id] = new Santa(firstX, firstY);
			}else {
				dead[id]=true;
			}
		}
		//맵에서 이동한 루돌프 위치 반영
		map[rx][ry]=-1;
	}
	
    static void inputTestcase() throws IOException {
        st = new StringTokenizer(br.readLine().trim());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        P = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        D = Integer.parseInt(st.nextToken());

        st = new StringTokenizer(br.readLine().trim());
        rx = Integer.parseInt(st.nextToken())-1;
        ry = Integer.parseInt(st.nextToken())-1;

        map = new int[N][N];
        santa = new Santa[P + 1];
        stun = new int[P + 1];
        dead = new boolean[P + 1];
        score = new int[P + 1];
        
        //루돌프 초기 위치 표시
        map[rx][ry]=-1;
        
        //산타 초기 위치 표시
        for (int idx = 0; idx < P; idx++) {
            st = new StringTokenizer(br.readLine().trim());
            int num = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken())-1;
            int y = Integer.parseInt(st.nextToken())-1;
            santa[num] = new Santa(x, y);
            map[x][y]=num;
        }
    }
    
    static void moveSanta() {
		for(int idx=1; idx<=P; idx++) {
			//탈락하거나 기절한 산타는 넘어간다.
			if(dead[idx] || stun[idx]>0) {
				continue;
			}
			//4방향 중 루돌프가 가장 가까운 방향 찾기
			//루돌프와의 거리
			int minDist = (int) (Math.pow(santa[idx].x - rx, 2) + Math.pow(santa[idx].y - ry, 2));
			int moveDir = -1;
			for (int d = 0; d < 4; d++) {
				int nx = santa[idx].x + dx[d];
				int ny = santa[idx].y + dy[d];
				
				// 맵범위 밖이거나 다른 산타가 있는 경우 넘어감
				if (!isRange(nx, ny) || map[nx][ny] > 0) {
					continue;
				}
				
				int dist = (int) (Math.pow(nx - rx, 2) + Math.pow(ny - ry, 2));
				
				if (dist < minDist) {
					minDist = dist;
					moveDir = d;
				}
			}
			
			//이동했다면
			if(moveDir!=-1) {
				int nx = santa[idx].x + dx[moveDir];
				int ny = santa[idx].y + dy[moveDir];
				
				//이동 후 루돌프와 충돌했다면
				if (nx == rx && ny == ry) {
					stun[idx] = 2; //산타 기절
					
					int moveX = -dx[moveDir];
					int moveY = -dy[moveDir];
					
					int firstX = nx+moveX*D;
					int firstY = ny+moveY*D;
					
					int lastX = firstX;
					int lastY = firstY;
					
					//한칸만 밀려난다면 점수만 증가
					if(D==1) {
						score[idx]+=D;
					}
					else {
						//밀려난 칸에 다른 산타가 있다면
						while (isRange(lastX, lastY) && map[lastX][lastY] > 0) {
							//연쇄적으로 한 칸씩 밀려남
							lastX += moveX;
							lastY += moveY;
						}
						//충돌이 발생한 마지막 위치부터 산타 이동
						while(!(lastX == firstX && lastY == firstY)) {
							int prevX = lastX - moveX;
							int prevY = lastY - moveY;
							
							//맵범위를 벗어나면
							if(!isRange(prevX, prevY)) {
								break;
							}
							
							int idx2=map[prevX][prevY];
							if(isRange(lastX, lastY)) {
								map[lastX][lastY]=idx2;
								santa[idx2]=new Santa(lastX, lastY);
							}else {
								dead[idx2]=true;
							}
							lastX=prevX;
							lastY=prevY;
						}
						//충돌한 산타 점수 증가
						score[idx]+=D;
						//맵에 이동 반영
						map[santa[idx].x][santa[idx].y]=0;
						if (isRange(firstX, firstY)) {
							map[firstX][firstY] = idx;
							santa[idx] = new Santa(firstX, firstY);
						} else {
							dead[idx] = true;
						}
					}
					
				}
				//이동 후 루돌프와 충돌하지 않았다면
				else {
					//맵에서 원래 위치를 빈칸으로 변경
					map[santa[idx].x][santa[idx].y]=0;
					santa[idx] = new Santa(nx, ny);
					//맵에 새로운 위치 변경
					map[nx][ny]=idx;
				}
			}
		}
	}
    
	static void decreaseStunTurn() {
		for(int idx=1; idx<=P; idx++) {
			if(stun[idx]>0) {
				stun[idx]--;
			}
		}
	}
	
	static void giveSurvivalScore() {
		for(int idx=1; idx<=P; idx++) {
			if(!dead[idx]) {
				score[idx]++;
			}
		}
	}
	
	static boolean isRange(int x, int y) {
		return x>=0 && x<N && y>=0 && y<N;
	}
	
}